package pl.thinkblue.example.session.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import java.util.UUID;

import pl.thinkblue.example.session.MainActivity;
import pl.thinkblue.example.session.SessionUpdateReceiver;

/**
 * The SessionController manages the session. The logic of the session:
 * - Session lasts as long as is not suspended
 * - Once the session is suspended it is still valid for "suspension timeout" amount of time
 * - Next time the "updateSession" will be called, the previous session can be resumed
 *   in case the suspension time is not over. Otherwise the previous session will be invalidated
 *   and the new one will be created
 *
 * Developer can listen to all the session updates by creating a BroadcastReceiver and register it
 * using the {@link #registerSessionUpdatesReceiver(Context, BroadcastReceiver)} method.
 *
 * For example of BroadcastReceiver implementation see the {@link SessionUpdateReceiver} class.
 *
 * For example of usage of the SessionController, see the {@link MainActivity#onResume()}
 * and {@link MainActivity#onStop()} methods.
 *
 * @author https://github.com/wkukielczak
 */
public class SessionController {
    public static final String SHARED_PREFERENCES_NAME = "Session";
    public static final int SHARED_PREFERENCES_DEFAULT_ACCESS_MODE = Context.MODE_PRIVATE;

    public static final String SP_KEY_SESSION = "session";

    public static final String INTENT_ACTION_NAME = "SessionUpdateReceiver:UpdateReceived";
    public static final String INTENT_CATEGORY_NAME = "D360Sdk:services:broadcasts";
    public static final String INTENT_EXTRA_SESSION = "session";
    public static final String INTENT_EXTRA_UPDATE_TYPE = "updateType";

    private Session mSession;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private long mDefaultBackgroundTimeout;

    /**
     * @param context Application context
     * @param defaultBackgroundTimeout Default background timeout expressed in milliseconds
     */
    public SessionController(Context context, long defaultBackgroundTimeout) {
        mDefaultBackgroundTimeout = defaultBackgroundTimeout;

        mContext = context;
        mSharedPreferences = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                SHARED_PREFERENCES_DEFAULT_ACCESS_MODE
        );
    }

    /**
     * Helper for registering broadcast receivers only for the SessionController
     *
     * @param receiver BroadcastReceiver instance
     */
    public static void registerSessionUpdatesReceiver(Context context, BroadcastReceiver receiver) {
        IntentFilter updateReceiverFilter = createIntentFilter();
        LocalBroadcastManager
                .getInstance(context)
                .registerReceiver(receiver, updateReceiverFilter);
    }

    /**
     * The session logic. This is on of two only places allowed to broadcast session updates
     */
    public void updateSession() {
        mSession = retrieveExistingSession();

        // Session exists, but it's invalid
        if (null != mSession) {
            validateSession();

            if (!mSession.isValid()) {
                // Broadcast that the previous session has been invalidated
                broadcastSessionUpdate(mSession, UpdateType.UPDATE);

                // Create a new session and broadcast it
                mSession = createSession();
                persistSessionInStorage(mSession);

                broadcastSessionUpdate(mSession, UpdateType.CREATE);
            } else {
                mSession.setUpdatedTimestamp(getTimestamp());
                persistSessionInStorage(mSession);

                broadcastSessionUpdate(mSession, UpdateType.UPDATE);
            }
        } else {
            mSession = createSession();
            persistSessionInStorage(mSession);
            broadcastSessionUpdate(mSession, UpdateType.CREATE);
        }
    }

    /**
     * Suspend the session. From this point of time the session may become invalid, if the
     * "suspension timeout" will be reached. This is on of two only places allowed to broadcast
     * session updates
     */
    public void suspendSession() {
        mSession = retrieveExistingSession();

        if (null != mSession) {
            mSession.setSuspended(true);
            persistSessionInStorage(mSession);
            broadcastSessionUpdate(mSession, UpdateType.UPDATE);
        }

    }

    /**
     * Retrieve existing session object. First check if the session is stored in RAM. If not, then
     * try to recover session saved in the app's storage
     *
     * @return Session object or null if there is no existing session stored
     */
    private Session retrieveExistingSession() {
        // First, check if there is a session object persisted in the RAM
        if (null != mSession) {
            return mSession;
        } else {
            return retrievePersistedSession();
        }
    }

    /**
     * Build a new Session object
     *
     * @return Session object
     */
    private Session createSession() {
        Session session = new Session();
        session
                .setId(UUID.randomUUID().toString())
                .setSuspensionTimeout(mDefaultBackgroundTimeout)
                .setUpdatedTimestamp(getTimestamp())
                .setValid(true);

        return session;
    }

    /**
     * Save provided Session object in the app's storage
     *
     * @param session Session object to be saved
     */
    private void persistSessionInStorage(Session session) {
        if (null != session) {
            mSharedPreferences
                    .edit()
                    .putString(SP_KEY_SESSION, session.toString())
                    .apply();
        }
    }

    /**
     * Look for the serialized session in the app's storage.
     * This is a backup in case the app will crash
     *
     * @return Session object or null if there is no on saved in the app's storage
     */
    private Session retrievePersistedSession() {
        Session session = null;
        String serializedSession = mSharedPreferences.getString(SP_KEY_SESSION, null);

        if (null != serializedSession) {
            session = Session.fromString(serializedSession);
        }

        return session;
    }

    /**
     * Check if the existing session is still valid. Change the session's state in the
     * Session object
     */
    private void validateSession() {
        // Validate the session only if it's suspended
        if (null != mSession && mSession.isSuspended()) {
            long maximumTimeToLive = mSession.getUpdatedTimestamp() + mSession.getSuspensionTimeout();
            long currentTimestamp = getTimestamp();
            boolean isValid = maximumTimeToLive > currentTimestamp;
            mSession.setValid(isValid);

            if (isValid) {
                mSession.setSuspended(false);
            }

            persistSessionInStorage(mSession);
        }
    }

    private void broadcastSessionUpdate(Session session, UpdateType updateType) {
        Intent sessionUpdate = null;

        if (null != session) {
            sessionUpdate = new Intent(INTENT_ACTION_NAME);
            sessionUpdate.addCategory(INTENT_CATEGORY_NAME);
            sessionUpdate.putExtra(INTENT_EXTRA_SESSION, session.toString());
            sessionUpdate.putExtra(INTENT_EXTRA_UPDATE_TYPE, updateType.name());
        }

        if (null != sessionUpdate) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(sessionUpdate);
        }
    }

    private static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(INTENT_ACTION_NAME);
        intentFilter.addCategory(INTENT_CATEGORY_NAME);

        return intentFilter;
    }

    private long getTimestamp() {
        return System.currentTimeMillis();
    }

}
