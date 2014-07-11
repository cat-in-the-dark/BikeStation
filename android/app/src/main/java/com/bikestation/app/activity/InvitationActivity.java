package com.bikestation.app.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.bikestation.app.account.AccountAuthenticator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAccountInvite(getIntent());
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }

    private void checkAccountInvite(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String link = intent.getData().getScheme()
                    + intent.getData().getSchemeSpecificPart();
            Map<String, List<String>> params = getQueryParams(link);
            if (!params.containsKey("account") ||
                    !params.containsKey("lock") ||
                    !params.containsKey("password"))
                return;

            String type = AccountAuthenticator.ACCOUNT_TYPE;
            AccountManager manager = AccountManager.get(this);
            Account account = getAccountByName(manager, params.get("account").get(0));
            if (account == null){
                account = new Account(params.get("account").get(0),
                        AccountAuthenticator.ACCOUNT_TYPE);
            }
            Bundle bundle = new Bundle();
            bundle.putString(params.get("lock").get(0), params.get("password").get(0));
            manager.addAccountExplicitly(account, "password", bundle);
        }
    }

    public static Account getAccountByName(AccountManager manager, String name){
        Account[] accounts = manager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
        for (Account account : accounts){
            if (account.name.equals(name))
                return account;
        }
        return null;
    }

    public static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }
}
