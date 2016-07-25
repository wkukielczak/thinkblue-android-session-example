package pl.thinkblue.example.session;

import android.app.Application;

import pl.thinkblue.example.session.session.SessionController;

/**
 * @author https://github.com/wkukielczak
 */
public class ExampleApp extends Application {
    private SessionController mSessionController;

    @Override
    public void onCreate() {
        super.onCreate();

        // register the receiver first
        SessionController.registerSessionUpdatesReceiver(
                getApplicationContext(),
                new SessionUpdateReceiver()
        );

        // Start the session controller with 1 minute background session time
        mSessionController = new SessionController(getApplicationContext(), 60*1000);
    }

    public SessionController getSessionController() {
        return mSessionController;
    }
}
