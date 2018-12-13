/*
 * Copyright 2017 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.app.dataadapter.exception;

import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.DataAdapterError;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    /**
     * Handling of unexpected errors.
     * @param t Throwable.
     * @return Response with error information.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDefaultException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Data Adapter", t);
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.ERROR_GENERIC, "Unknown Error");
        return new ErrorResponse(error);
    }

    /**
     * Handling of authentication failures.
     * @param ex Authentication failure exception, with exception details.
     * @return Response with error information.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handleAuthenticationError(AuthenticationFailedException ex) {
        // regular authentication failed error
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.AUTHENTICATION_FAILED, ex.getMessage());
        error.setRemainingAttempts(ex.getRemainingAttempts());
        return new ErrorResponse(error);
    }

    /**
     * Handling of SMS OTP authorization failures.
     *
     * @param ex Authorization failure exception, with exception details.
     * @return Response with error information.
     */
    @ExceptionHandler(SMSAuthorizationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handleAuthenticationError(SMSAuthorizationFailedException ex) {
        // regular sms authorization failed error
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.SMS_AUTHORIZATION_FAILED, ex.getMessage());
        error.setRemainingAttempts(ex.getRemainingAttempts());
        return new ErrorResponse(error);
    }

    /**
     * Handling of validation errors.
     * @param ex Exception.
     * @return Response with error information.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleDefaultException(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();
        final List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        for (ObjectError objError: allErrors) {
            errorMessages.addAll(Arrays.asList(objError.getCodes()));
        }

        // preparation of user friendly error messages for the UI
        String message;
        if (errorMessages.contains("login.username.empty")) {
            if (errorMessages.contains("login.password.empty")) {
                message = "login.username.empty login.password.empty";
            } else {
                if (errorMessages.contains("login.password.long")) {
                    message = "login.username.empty login.password.long";
                } else {
                    message = "login.username.empty";
                }
            }
        } else {
            if (errorMessages.contains("login.password.empty")) {
                if (errorMessages.contains("login.username.long")) {
                    message = "login.password.empty login.username.long";
                } else {
                    message = "login.password.empty";
                }
            } else {
                if (errorMessages.contains("login.username.long")) {
                    if (errorMessages.contains("login.password.long")) {
                        message = "login.username.long login.password.long";
                    } else {
                        message = "login.username.long";
                    }
                } else {
                    if (errorMessages.contains("login.password.long")) {
                        message = "login.password.long";
                    } else {
                        message = "login.authenticationFailed";
                    }
                }
            }
        }

        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.INPUT_INVALID, message);
        error.setValidationErrors(errorMessages);
        return new ErrorResponse(error);
    }

    /**
     * Handling of user not found exception.
     * @param ex Exception.
     * @return Response with error information.
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.INPUT_INVALID, ex.getMessage());
        return new ErrorResponse(error);
    }

    /**
     * Handling of invalid operation context exception.
     * @param ex Exception.
     * @return Response with error information.
     */
    @ExceptionHandler(InvalidOperationContextException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidOperationContextException(InvalidOperationContextException ex) {
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.OPERATION_CONTEXT_INVALID, ex.getMessage());
        return new ErrorResponse(error);
    }

    /**
     * Handling of exceptions occurring during communication with remote backends.
     * @param ex Exception.
     * @return Response with error information.
     */
    @ExceptionHandler(DataAdapterRemoteException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDataAdapterRemoteException(DataAdapterRemoteException ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred while communicating with remote system", ex);
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.REMOTE_ERROR, "error.remote");
        return new ErrorResponse(error);
    }

}