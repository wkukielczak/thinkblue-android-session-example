package pl.thinkblue.example.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pl.thinkblue.example.session.session.Session;
import pl.thinkblue.example.session.session.SessionController;
import pl.thinkblue.example.session.session.UpdateType;

/**
 * Sample implementation of the Session updates receiver
 *
 * @author https://github.com/wkukielczak
 */
public class SessionUpdateReceiver extends BroadcastReceiver {
    public static final String TAG = "SessionUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateType updateType = getUpdateType(intent);
        Session session = getSession(intent);

        if(null != updateType && null != session) {
            switch (updateType) {
                // Simple case: new session is just created. In case of 360 SDK: send session_start
                case CREATE:
                    Log.i(TAG, "START of the session.");
                    Log.i(TAG, "Session (id:" + session.getId() + ") created. " +
                            "Object dump: " + session.toString());
                    break;

                // Several cases mixed
                case UPDATE:
                    // Session became suspended. In case of 360 SDK - we don't care (no events are sent)
                    if (session.isValid() && session.isSuspended()) {
                        Log.i(TAG, "Session (id:" + session.getId() + ") suspended. " +
                                "Object dump: " + session.toString());
                    }

                    // Session is restored. In case of 360 SDK - we don't care (no events are sent)
                    if (session.isValid() && !session.isSuspended()) {
                        Log.i(TAG, "Session (id:" + session.getId() + ") active. " +
                                "Object dump: " + session.toString());
                    }

                    // Session is no longer valid. In case of 360 SDK - send session_end event
                    if (!session.isValid()) {
                        Log.i(TAG, "Session (id:" + session.getId() + ") invalidated! " +
                                "Object dump: " + session.toString());
                        Log.i(TAG, "------ END of the session, the new one should now be created");
                    }
                    break;
            }
        }
    }

    /**
     * Helper to get a session object from the intent
     *
     * @param intent Intent provided to the {@link #onReceive(Context, Intent)} method
     * @return Session object or null if can't be retrieved
     */
    private Session getSession(Intent intent) {
        Session session = null;
        if (null != intent.getExtras()) {
            if (intent.hasExtra(SessionController.INTENT_EXTRA_SESSION)) {
                session = Session.fromString(
                        intent.getStringExtra(SessionController.INTENT_EXTRA_SESSION)
                );
            }
        }

        return session;
    }

    /**
     * Convert update type received from the broadcast. As the Intent's extras don't allows us to
     * send enum, we just convert it's String value to the actual enum value
     *
     * @param intent Intent provided to the {@link #onReceive(Context, Intent)} method
     * @return UpdateType value or null by default
     */
    private UpdateType getUpdateType(Intent intent) {
        UpdateType updateType = null;
        if (
                null != intent.getExtras()
                        && intent.hasExtra(SessionController.INTENT_EXTRA_UPDATE_TYPE)
                ) {

            updateType = UpdateType.valueOf(
                    intent.getStringExtra(SessionController.INTENT_EXTRA_UPDATE_TYPE)
            );
        }

        return updateType;
    }
}
