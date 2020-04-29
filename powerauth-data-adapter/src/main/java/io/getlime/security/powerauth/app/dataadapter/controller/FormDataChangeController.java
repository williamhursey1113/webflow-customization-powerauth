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
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.DecorateOperationFormDataRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.FormDataChangeNotificationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.DecorateOperationFormDataResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class which handles notifications about changes of operation form data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/operation/formdata")
public class FormDataChangeController {

    private static final Logger logger = LoggerFactory.getLogger(FormDataChangeController.class);

    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public FormDataChangeController(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }

    /**
     * Receive a new operation form data change notification.
     *
     * @param request Request with change details.
     * @return Object response.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     */
    @PostMapping(value = "/change")
    public Response formDataChangedNotification(@RequestBody ObjectRequest<FormDataChangeNotificationRequest> request) throws DataAdapterRemoteException {
        logger.info("Received formDataChangedNotification request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        FormDataChangeNotificationRequest notification = request.getRequestObject();
        String userId = notification.getUserId();
        String organizationId = notification.getOrganizationId();
        OperationContext operationContext = notification.getOperationContext();
        FormDataChange formDataChange = notification.getFormDataChange();
        dataAdapter.formDataChangedNotification(userId, organizationId, formDataChange, operationContext);
        logger.debug("The formDataChangedNotification request succeeded");
        return new Response();
    }

    /**
     * Decorate operation form data.
     *
     * @param request Request with user ID.
     * @return Response with decorated operation form data.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws UserNotFoundException Thrown in case user is not found.
     */
    @PostMapping(value = "/decorate")
    public ObjectResponse<DecorateOperationFormDataResponse> decorateOperationFormData(@RequestBody ObjectRequest<DecorateOperationFormDataRequest> request) throws DataAdapterRemoteException, UserNotFoundException {
        logger.info("Received decorateOperationFormData request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        DecorateOperationFormDataRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        String organizationId = requestObject.getOrganizationId();
        AuthMethod authMethod = requestObject.getAuthMethod();
        OperationContext operationContext = requestObject.getOperationContext();
        DecorateOperationFormDataResponse response = dataAdapter.decorateFormData(userId, organizationId, authMethod, operationContext);
        logger.debug("The decorateOperationFormData request succeeded");
        return new ObjectResponse<>(response);
    }
}
