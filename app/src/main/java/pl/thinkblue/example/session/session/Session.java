package pl.thinkblue.example.session.session;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The session model
 *
 * @author https://github.com/wkukielczak
 */
public class Session {
    private String mId;
    private long mSuspensionTimeout;
    private long mUpdatedTimestamp;
    private boolean mIsSuspended;
    private boolean mIsValid;

    public String getId() {
        return mId;
    }

    public Session setId(String id) {
        mId = id;
        return this;
    }

    public long getSuspensionTimeout() {
        return mSuspensionTimeout;
    }

    public Session setSuspensionTimeout(long suspensionTimeout) {
        mSuspensionTimeout = suspensionTimeout;
        return this;
    }

    public long getUpdatedTimestamp() {
        return mUpdatedTimestamp;
    }

    public Session setUpdatedTimestamp(long updatedTimestamp) {
        mUpdatedTimestamp = updatedTimestamp;
        return this;
    }

    public boolean isSuspended() {
        return mIsSuspended;
    }

    public Session setSuspended(boolean suspended) {
        mIsSuspended = suspended;
        return this;
    }

    public boolean isValid() {
        return mIsValid;
    }

    public Session setValid(boolean valid) {
        mIsValid = valid;
        return this;
    }

    @Override
    public String toString() {
        String result = super.toString();

        try {
            JSONObject session = new JSONObject();
            session.put("id", mId);
            session.put("backgroundTimeout", mSuspensionTimeout);
            session.put("updatedTimestamp", mUpdatedTimestamp);
            session.put("isSuspended", mIsSuspended);
            session.put("isValid", mIsValid);

            result = session.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Session fromString(String input) {
        Session session = null;

        try {
            JSONObject sessionJson = new JSONObject(input);

            if (sessionJson.has("id")) {
                session = new Session();
                // Note that the fallback values will always cause the session object invalid!
                session
                        .setId(sessionJson.optString("id", null))
                        .setSuspensionTimeout(sessionJson.optLong("backgroundTimeout", 0))
                        .setUpdatedTimestamp(sessionJson.optLong("updatedTimestamp", 0))
                        .setSuspended(sessionJson.optBoolean("isSuspended", false))
                        .setValid(sessionJson.optBoolean("isValid", false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            session = null;
        }

        return session;
    }
}
