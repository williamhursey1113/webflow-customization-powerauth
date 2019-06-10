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
package io.getlime.security.powerauth.app.dataadapter.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.UserNotFoundException;
import io.getlime.security.powerauth.app.dataadapter.impl.validation.AuthenticationRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserDetailRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller class which handles user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/auth/user")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param requestValidator Validator for authentication requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public AuthenticationController(AuthenticationRequestValidator requestValidator, DataAdapter dataAdapter) {
        this.requestValidator = requestValidator;
        this.dataAdapter = dataAdapter;
    }

    /**
     * Initializes the request validator.
     * @param binder Data binder.
     */
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(requestValidator);
    }

    /**
     * Authenticate user with given username and password.
     *
     * @param request Authenticate user request.
     * @param result BindingResult for input validation.
     * @return Response with authenticated user ID.
     * @throws MethodArgumentNotValidException Thrown in case form parameters are not valid.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws AuthenticationFailedException Thrown in case that authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ObjectResponse<AuthenticationResponse> authenticate(@Valid @RequestBody ObjectRequest<AuthenticationRequest> request, BindingResult result) throws MethodArgumentNotValidException, DataAdapterRemoteException, AuthenticationFailedException {
        if (result.hasErrors()) {
            // Call of getEnclosingMethod() on local class returns a reference to current method
            class Local {}
            MethodParameter methodParam = new MethodParameter(Local.class.getEnclosingMethod(), 0);
            logger.warn("The authenticate request failed due to validation errors");
            throw new MethodArgumentNotValidException(methodParam, result);
        }
        logger.info("Received authenticate request, username: {}, operation ID: {}", request.getRequestObject().getUsername(), request.getRequestObject().getOperationContext().getId());
        AuthenticationRequest authenticationRequest = request.getRequestObject();
        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();
        OperationContext operationContext = authenticationRequest.getOperationContext();
        UserDetailResponse userDetailResponse = dataAdapter.authenticateUser(username, password, operationContext);
        AuthenticationResponse response = new AuthenticationResponse(userDetailResponse.getId());
        logger.info("The authenticate request succeeded, user ID: {}, operation ID: {}", request.getRequestObject().getUsername(), request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Fetch user details based on user ID.
     *
     * @param request Request with user ID.
     * @return Response with user details.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws UserNotFoundException Thrown in case user is not found.
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public ObjectResponse<UserDetailResponse> fetchUserDetail(@RequestBody ObjectRequest<UserDetailRequest> request) throws DataAdapterRemoteException, UserNotFoundException {
        logger.info("Received fetchUserDetail request, user ID: {}", request.getRequestObject().getId());
        UserDetailRequest userDetailRequest = request.getRequestObject();
        String userId = userDetailRequest.getId();
        UserDetailResponse response = dataAdapter.fetchUserDetail(userId);
        logger.info("The fetchUserDetail request succeeded");
        return new ObjectResponse<>(response);
    }


}
