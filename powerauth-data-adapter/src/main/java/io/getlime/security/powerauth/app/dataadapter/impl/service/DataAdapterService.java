package io.getlime.security.powerauth.app.dataadapter.impl.service;

import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.*;
import io.getlime.security.powerauth.app.dataadapter.service.DataAdapterI18NService;
import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.*;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.AmountAttribute;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.Attribute;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.FormFieldConfig;
import io.getlime.security.powerauth.lib.dataadapter.model.response.DecorateOperationFormDataResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample implementation of DataAdapter interface which should be updated in real implementation.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class DataAdapterService implements DataAdapter {

    private static final String BANK_ACCOUNT_CHOICE_ID = "operation.bankAccountChoice";

    private final DataAdapterI18NService dataAdapterI18NService;
    private final OperationValueExtractionService operationValueExtractionService;

    public DataAdapterService(DataAdapterI18NService dataAdapterI18NService, OperationValueExtractionService operationValueExtractionService) {
        this.dataAdapterI18NService = dataAdapterI18NService;
        this.operationValueExtractionService = operationValueExtractionService;
    }

    @Override
    public UserDetailResponse authenticateUser(String username, String password, OperationContext operationContext) throws DataAdapterRemoteException, AuthenticationFailedException {
        // Here will be the real authentication - call to the backend providing authentication.
        // In case that authentication fails, throw an AuthenticationFailedException.
        if ("test".equals(password)) {
            try {
                return fetchUserDetail(username);
            } catch (UserNotFoundException e) {
                throw new AuthenticationFailedException("login.authenticationFailed");
            }
        }
        AuthenticationFailedException authFailedException = new AuthenticationFailedException("login.authenticationFailed");
        // Set number of remaining attempts for this userId in case it is available.
        // authFailedException.setRemainingAttempts(5);

        // Use the following code to let the user know that the account has been blocked temporarily.
        // final AuthenticationFailedException authFailedException = new AuthenticationFailedException("login.authenticationBlocked");
        // authFailedException.setRemainingAttempts(0);

        throw authFailedException;
    }

    @Override
    public UserDetailResponse fetchUserDetail(String userId) throws DataAdapterRemoteException, UserNotFoundException {
        // Fetch user details here ...
        // In case that user is not found, throw a UserNotFoundException.
        UserDetailResponse responseObject = new UserDetailResponse();
        responseObject.setId(userId);
        responseObject.setGivenName("John");
        responseObject.setFamilyName("Doe");
        return responseObject;
    }

    @Override
    public DecorateOperationFormDataResponse decorateFormData(String userId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException {
        String operationName = operationContext.getName();
        FormData formData = operationContext.getFormData();
        // Fetch bank account list for given user here from the bank backend.
        // In case that user is not found, throw a UserNotFoundException.
        // Replace mock bank account data with real data loaded from the bank backend.
        // In case the bank account selection is disabled, return an empty list.

        if (!"authorize_payment".equals(operationName)) {
            // return empty list for operations other than authorize_payment
            return new DecorateOperationFormDataResponse(formData);
        }

        List<BankAccount> bankAccounts = new ArrayList<>();

        BankAccount bankAccount1 = new BankAccount();
        bankAccount1.setName("Běžný účet v CZK");
        bankAccount1.setBalance(new BigDecimal("24394.52"));
        bankAccount1.setNumber("12345678/1234");
        bankAccount1.setAccountId("CZ4012340000000012345678");
        bankAccount1.setCurrency("CZK");
        bankAccounts.add(bankAccount1);

        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setName("Spořící účet v CZK");
        bankAccount2.setBalance(new BigDecimal("158121.10"));
        bankAccount2.setNumber("87654321/4321");
        bankAccount2.setAccountId("CZ4043210000000087654321");
        bankAccount2.setCurrency("CZK");
        bankAccounts.add(bankAccount2);

        BankAccount bankAccount3 = new BankAccount();
        bankAccount3.setName("Spořící účet v EUR");
        bankAccount3.setBalance(new BigDecimal("1.90"));
        bankAccount3.setNumber("44444444/1111");
        bankAccount3.setAccountId("CZ4011110000000044444444");
        bankAccount3.setCurrency("EUR");
        bankAccount3.setUsableForPayment(false);
        bankAccount3.setUnusableForPaymentReason(dataAdapterI18NService.messageSource().getMessage("operationReview.balanceTooLow", null, LocaleContextHolder.getLocale()));
        bankAccounts.add(bankAccount3);

        boolean choiceEnabled = true;
        String defaultValue = "CZ4012340000000012345678";

        List<FormFieldConfig> configs = formData.getConfig();
        for (FormFieldConfig config: configs) {
            if ("operation.bankAccountChoice".equals(config.getId())) {
                choiceEnabled = config.isEnabled();
                // You should check the default value against list of available accounts.
                defaultValue = config.getDefaultValue();
            }
        }
        Attribute attr = formData.addBankAccountChoice(BANK_ACCOUNT_CHOICE_ID, bankAccounts, choiceEnabled, defaultValue);

        // Sample warning banner displayed above the bank account choice field.
        // formData.addBannerBeforeField(BannerType.BANNER_WARNING, "banner.invalidAccount", attr);

        return new DecorateOperationFormDataResponse(formData);
    }

    @Override
    public void formDataChangedNotification(String userId, FormDataChange change, OperationContext operationContext) throws DataAdapterRemoteException {
        String operationId = operationContext.getId();
        switch (change.getType()) {
            case BANK_ACCOUNT_CHOICE:
                // Handle bank account choice here (e.g. send notification to bank backend).
                BankAccountChoice bankAccountChoice = (BankAccountChoice) change;
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Bank account chosen: {0}, operation ID: {1}", new String[] {bankAccountChoice.getBankAccountId(), operationContext.getId()});
                break;
            case AUTH_METHOD_CHOICE:
                // Handle authorization method choice here (e.g. send notification to bank backend).
                AuthMethodChoice authMethodChoice = (AuthMethodChoice) change;
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Authorization method chosen: {0}, operation ID: {1}", new String[] {authMethodChoice.getChosenAuthMethod().toString(), operationContext.getId()});
                break;
            default:
                throw new IllegalStateException("Invalid change entity type: " + change.getType());
        }
    }

    @Override
    public void operationChangedNotification(String userId, OperationChange change, OperationContext operationContext) throws DataAdapterRemoteException {
        String operationId = operationContext.getId();
        // Handle operation change here (e.g. send notification to bank backend).
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation changed, status: {0}, operation ID: {1}", new String[] {change.toString(), operationContext.getId()});
    }

    @Override
    public AuthorizationCode generateAuthorizationCode(String userId, OperationContext operationContext) throws InvalidOperationContextException {
        String operationName = operationContext.getName();
        List<String> digestItems = new ArrayList<>();
        switch (operationName) {
            case "login": {
                digestItems.add(operationName);
                break;
            }
            case "authorize_payment": {
                AmountAttribute amountAttribute = operationValueExtractionService.getAmount(operationContext);
                String account = operationValueExtractionService.getAccount(operationContext);
                BigDecimal amount = amountAttribute.getAmount();
                String currency = amountAttribute.getCurrency();
                digestItems.add(amount.toPlainString());
                digestItems.add(currency);
                digestItems.add(account);
                break;
            }
            // Add new operations here.
            default:
                throw new InvalidOperationContextException("Unsupported operation: " + operationName);
        }

        final DataDigest.Result digestResult = new DataDigest().generateDigest(digestItems);
        return new AuthorizationCode(digestResult.getDigest(), digestResult.getSalt());
    }

    @Override
    public String generateSMSText(String userId, OperationContext operationContext, AuthorizationCode authorizationCode, String lang) throws InvalidOperationContextException {
        String operationName = operationContext.getName();
        String[] messageArgs;
        switch (operationName) {
            case "login": {
                messageArgs = new String[]{authorizationCode.getCode()};
                break;
            }
            case "authorize_payment": {
                AmountAttribute amountAttribute = operationValueExtractionService.getAmount(operationContext);
                String account = operationValueExtractionService.getAccount(operationContext);
                BigDecimal amount = amountAttribute.getAmount();
                String currency = amountAttribute.getCurrency();
                messageArgs = new String[]{amount.toPlainString(), currency, account, authorizationCode.getCode()};
                break;
            }
            // Add new operations here.
            default:
                throw new InvalidOperationContextException("Unsupported operation: " + operationName);
        }

        return dataAdapterI18NService.messageSource().getMessage(operationName + ".smsText", messageArgs, new Locale(lang));
    }

    @Override
    public void sendAuthorizationSMS(String userId, String messageText,  OperationContext operationContext) throws DataAdapterRemoteException, SMSAuthorizationFailedException {
        // Add here code to send the SMS OTP message to user identified by userId with messageText.
        // In case message delivery fails, throw an SMSAuthorizationFailedException.
    }

}
