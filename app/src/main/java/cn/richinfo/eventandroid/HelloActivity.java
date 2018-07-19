package cn.richinfo.eventandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import event.router.annotation.Router;

@Router(path = "/test/hello", type = Router.Type.COMPONENT_ACTIVITY)
public class HelloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
    }
}
