package com.lucasasselli.evernotewear;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntro2;
public class SetupActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {
        // This is to fix a bad layout in AppIntro library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        addSlide(SetupFragment.newInstance(getString(R.string.setup_slide_welcome_title), getString(R.string.setup_slide_welcome_description), R.drawable.pic_slide_welcome));
        addSlide(SetupFragment.newInstance(getString(R.string.setup_slide_login_title), getString(R.string.setup_slide_login_description), R.drawable.pic_slide_welcome));
        addSlide(SetupFragment.newInstance(getString(R.string.setup_slide_alldone_title), getString(R.string.setup_slide_alldone_description), R.drawable.pic_slide_alldone));
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button
    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button
    }

    public static class SetupFragment extends Fragment {

        private final static String ARGUMENT_TITLE = "ARGUMENT_TITLE";
        private final static String ARGUMENT_DESC = "ARGUMENT_DESC";
        private final static String ARGUMENT_DRAWABLE = "ARGUMENT_DRAWABLE";

        private int drawable;
        private String title;
        private String description;



        public static SetupFragment newInstance(String title, String description, int imageDrawable) {

            SetupFragment setupFragment = new SetupFragment();

            Bundle bundle = new Bundle();
            bundle.putCharSequence(ARGUMENT_TITLE, title);
            bundle.putCharSequence(ARGUMENT_DESC, description);
            bundle.putInt(ARGUMENT_DRAWABLE, imageDrawable);
            setupFragment.setArguments(bundle);

            return setupFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null && bundle.size() != 0) {
                drawable = bundle.getInt(ARGUMENT_DRAWABLE);
                title = getArguments().getString(ARGUMENT_TITLE);
                description = bundle.getString(ARGUMENT_DESC);
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_setup_generic, container, false);

            TextView titleText = (TextView) view.findViewById(R.id.slide_title_text);
            titleText.setText(title);

            TextView descriptionText = (TextView) view.findViewById(R.id.slide_description_text);
            descriptionText.setText(description);

            ImageView imageView = (ImageView) view.findViewById(R.id.slide_image);
            imageView.setImageResource(drawable);

            return view;
        }
    }
}
