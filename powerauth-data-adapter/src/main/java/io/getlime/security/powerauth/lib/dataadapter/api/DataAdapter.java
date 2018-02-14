/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.api;

import io.getlime.security.powerauth.lib.dataadapter.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.lib.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;

/**
 * Interface defines methods which should be implemented for integration of Web Flow with 3rd parties.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public interface DataAdapter {

    /**
     * Authenticate user using provided credentials.
     *
     * @param username Username for user authentication.
     * @param password Password for user authentication.
     * @return UserDetailResponse Response with user details.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws AuthenticationFailedException Thrown when authentication fails.
     */
    UserDetailResponse authenticateUser(String username, String password) throws DataAdapterRemoteException, AuthenticationFailedException;

    /**
     * Fetch user detail for given user.
     * @param userId User ID.
     * @return Response with user details.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws UserNotFoundException Thrown when user does not exist.
     */
    UserDetailResponse fetchUserDetail(String userId) throws DataAdapterRemoteException, UserNotFoundException;

    /**
     * Fetch bank account details for given user.
     * @param userId User ID.
     * @param operationName Operation name.
     * @param operationId Operation ID.
     * @param formData Operation form data.
     * @return Response with bank account details.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws UserNotFoundException Thrown when user does not exist.
     */
    BankAccountListResponse fetchBankAccounts(String userId, String operationName, String operationId, OperationFormData formData) throws DataAdapterRemoteException, UserNotFoundException;

    /**
     * Receive notification about formData change.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @param formDataChange FormData change.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    void formDataChangedNotification(String userId, String operationId, FormDataChange formDataChange) throws DataAdapterRemoteException;

    /**
     * Receive notification about operation change.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @param operationChange Operation change.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    void operationChangedNotification(String userId, String operationId, OperationChange operationChange) throws DataAdapterRemoteException;

    /**
     * Send an authorization SMS with generated OTP.
     * @param userId User ID.
     * @param messageText Text of SMS message.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws SMSAuthorizationFailedException Thrown when message could not be created.
     */
    void sendAuthorizationSMS(String userId, String messageText) throws DataAdapterRemoteException, SMSAuthorizationFailedException;

}
