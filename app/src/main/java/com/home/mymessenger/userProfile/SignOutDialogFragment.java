package com.home.mymessenger.userProfile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.home.mymessenger.dp.RealmHelper;
import com.home.mymessenger.loginsignin.LogInActivity;

import io.realm.Realm;

public class SignOutDialogFragment extends AppCompatDialogFragment {

    private Realm realm = RealmHelper.getInstance().getRealm();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sign out?");
        builder.setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, id) -> signOut())
                .setNegativeButton("No", (dialog, id) -> {

                });
        return builder.create();
    }


    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        realm.executeTransaction(realm -> realm.deleteAll());

        Intent intent = new Intent(getActivity(), LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();

    }
}
