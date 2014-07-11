package com.bikestation.app.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    public final static String ACCOUNT_TYPE = "com.kdravolin.smartlock.app";
    private Context mContext;
    private Handler mHandler;

    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
        mHandler = new Handler();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Bundle result = new Bundle();
        final Intent intent;

//        AccountManager accountManager = AccountManager.get(mContext);
//        Account[] accounts = accountManager.getAccountsByType(accountType);
//        if (accounts.length > 0) {
//            final String message = mContext.getString(R.string.one_account_allowed);
//            result.putInt(AccountManager.KEY_ERROR_CODE, 0);
//            result.putString(AccountManager.KEY_ERROR_MESSAGE, message);
//
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
//                }
//            });
//        } else {
//            intent = new Intent(this.mContext, AccountActivity.class);
//            intent.putExtra(accountType, authTokenType);
//            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
//            result.putParcelable(AccountManager.KEY_INTENT, intent);
//        }
        return result;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

}
