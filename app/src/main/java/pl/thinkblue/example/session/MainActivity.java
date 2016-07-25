package pl.thinkblue.example.session;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * @author https://github.com/wkukielczak
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ExampleApp)getApplication()).getSessionController().updateSession();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ((ExampleApp)getApplication()).getSessionController().suspendSession();
    }
}
