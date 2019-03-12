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
package io.getlime.security.powerauth.app.dataadapter.impl.validation;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.app.dataadapter.impl.service.OperationValueExtractionService;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.AmountAttribute;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSMSAuthorizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

/**
 * Validator for SMS OTP authorization requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class CreateSMSAuthorizationRequestValidator implements Validator {

    private OperationValueExtractionService operationValueExtractionService;

    /**
     * Validator constructor.
     * @param operationValueExtractionService Operation form data service.
     */
    @Autowired
    public CreateSMSAuthorizationRequestValidator(OperationValueExtractionService operationValueExtractionService) {
        this.operationValueExtractionService = operationValueExtractionService;
    }

    /**
     * Return whether validator can validate given class.
     * @param clazz Validated class.
     * @return Whether validator can validate given class.
     */
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ObjectRequest.class.isAssignableFrom(clazz);
    }

    /**
     * Validate object and add validation errors.
     * @param o Validated object.
     * @param errors Errors object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void validate(@Nullable Object o, @NonNull Errors errors) {
        ObjectRequest<CreateSMSAuthorizationRequest> requestObject = (ObjectRequest<CreateSMSAuthorizationRequest>) o;
        if (requestObject == null) {
            errors.rejectValue("requestObject.operationContext", "operationContext.missing");
            return;
        }
        CreateSMSAuthorizationRequest authRequest = requestObject.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        String userId = authRequest.getUserId();
        String organizationId = authRequest.getOrganizationId();
        OperationContext operationContext = authRequest.getOperationContext();
        if (operationContext == null) {
            errors.rejectValue("requestObject.operationContext", "operationContext.missing");
        }

        String operationName = authRequest.getOperationContext().getName();

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.userId", "smsAuthorization.userId.empty");
        if (userId != null && userId.length() > 30) {
            errors.rejectValue("requestObject.userId", "smsAuthorization.userId.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.organizationId", "smsAuthorization.organizationId.empty");
        if (organizationId != null && organizationId.length() > 256) {
            errors.rejectValue("requestObject.organizationId", "smsAuthorization.organizationId.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.operationContext.name", "smsAuthorization.operationName.empty");
        if (operationName != null && operationName.length() > 32) {
            errors.rejectValue("requestObject.operationContext.name", "smsAuthorization.operationName.long");
        }

        if (operationName != null) {
            switch (operationName) {
                case "login":
                    // no field validation required
                    break;
                case "authorize_payment":
                    AmountAttribute amountAttribute;
                    try {
                        amountAttribute = operationValueExtractionService.getAmount(authRequest.getOperationContext());
                        if (amountAttribute == null) {
                            errors.rejectValue("requestObject.operationContext", "smsAuthorization.amount.empty");
                        } else {
                            BigDecimal amount = amountAttribute.getAmount();
                            String currency = amountAttribute.getCurrency();

                            if (amount == null) {
                                errors.rejectValue("requestObject.operationContext", "smsAuthorization.amount.empty");
                            } else if (amount.doubleValue() <= 0) {
                                errors.rejectValue("requestObject.operationContext", "smsAuthorization.amount.invalid");
                            }

                            if (currency == null || currency.isEmpty()) {
                                errors.rejectValue("requestObject.operationContext", "smsAuthorization.currency.empty");
                            }
                        }
                    } catch (InvalidOperationContextException ex) {
                        errors.rejectValue("requestObject.operationContext", "smsAuthorization.amount.empty");
                    }
                    String account;
                    try {
                        account = operationValueExtractionService.getAccount(authRequest.getOperationContext());
                        if (account == null || account.isEmpty()) {
                            errors.rejectValue("requestObject.operationContext", "smsAuthorization.account.empty");
                        }
                    } catch (InvalidOperationContextException ex) {
                        errors.rejectValue("requestObject.operationContext", "smsAuthorization.account.empty");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported operation in validator: " + operationName);
            }
        }
    }
}
