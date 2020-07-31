package com.yayandroid.locationmanager.providers.dialogprovider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.internal.ConnectionErrorMessages;

public class GoogleApiErrorDialogProvider {

    /**
     * From {@link GoogleApiAvailability#getErrorDialog(Activity, int, int, DialogInterface.OnCancelListener)}
     */
    public static Dialog getErrorDialog(
            @NonNull Activity activity,
            @NonNull GoogleApiAvailability instance,
            int gpServicesAvailability,
            int requestCode,
            DialogInterface.OnCancelListener cancelListener
    ) {
        Intent resolutionIntent = instance.getErrorResolutionIntent(activity, gpServicesAvailability, "d");

        return createDialog(activity, gpServicesAvailability, new DialogRedirect(resolutionIntent, activity, requestCode, cancelListener), cancelListener);
    }

    /**
     * From {@link GoogleApiAvailability#zaa(Context, int, com.google.android.gms.common.internal.DialogRedirect, DialogInterface.OnCancelListener)}
     */
    @SuppressWarnings("JavadocReference")
    @Nullable
    private static Dialog createDialog(Context context, int gpServicesAvailability, DialogRedirect dialogRedirect, DialogInterface.OnCancelListener cancelListener) {
        if (gpServicesAvailability == 0) {
            return null;
        } else {
            AlertDialog.Builder builder = null;

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.alertDialogTheme, typedValue, true);
            String resourceName = context.getResources().getResourceEntryName(typedValue.resourceId);

            if ("Theme.Dialog.Alert".equals(resourceName)) {
                builder = new AlertDialog.Builder(context, 5);
            }

            if (builder == null) {
                builder = new AlertDialog.Builder(context);
            }

            builder.setMessage(ConnectionErrorMessages.getErrorMessage(context, gpServicesAvailability));

            if (cancelListener != null) {
                builder.setOnCancelListener(cancelListener);
            }

            String positiveButtonText = ConnectionErrorMessages.getErrorDialogButtonMessage(context, gpServicesAvailability);

            builder.setPositiveButton(positiveButtonText, dialogRedirect);

            String title;

            if ((title = ConnectionErrorMessages.getErrorTitle(context, gpServicesAvailability)) != null) {
                builder.setTitle(title);
            }

            return builder.create();
        }
    }

    /**
     * From {@link com.google.android.gms.common.internal.zac}
     */
    @SuppressWarnings("JavadocReference")
    private static class DialogRedirect implements DialogInterface.OnClickListener {

        @Nullable
        private final Intent intent;

        @NonNull
        private final Activity activity;

        private final int requestCode;

        @NonNull
        private final DialogInterface.OnCancelListener cancelListener;

        DialogRedirect(
                @Nullable Intent intent,
                @NonNull Activity activity,
                int requestCode,
                @NonNull DialogInterface.OnCancelListener cancelListener
        ) {
            this.intent = intent;
            this.activity = activity;
            this.requestCode = requestCode;
            this.cancelListener = cancelListener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                this.redirect(dialog);
            } catch (ActivityNotFoundException e) {
                Log.e("DialogRedirect", "Failed to start resolution intent", e);
            } finally {
                dialog.dismiss();
            }
        }

        public final void redirect(DialogInterface dialog) {
            if (this.intent != null) {
                this.activity.startActivityForResult(this.intent, this.requestCode);
            } else {
                cancelListener.onCancel(dialog);
            }

        }

    }

}
