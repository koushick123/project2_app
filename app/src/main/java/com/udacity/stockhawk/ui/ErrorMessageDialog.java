package com.udacity.stockhawk.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Koushick on 22-02-2017.
 */
public class ErrorMessageDialog extends DialogFragment {

    @BindView(R.id.errorText)
    TextView error;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        builder.setMessage("No Stock Found");
        Dialog dialog = builder.create();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("errors", null);
        editor.apply();

        return dialog;
    }
}
