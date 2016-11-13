package org.atthack.november16.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.atthack.november16.R;
import org.atthack.november16.data.POI;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends BottomSheetDialogFragment{

    private OnFragmentInteractionListener mListener;
    POI point;

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onPlay();
        void onSpeech();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DetailFragment.OnFragmentInteractionListener) {
            mListener = (DetailFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }


    public DetailFragment() {

    }

    public void setData(POI poi) {
        // Empty constructor required for DialogFragment
        this.point = poi;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.poi_info_contents, container);
        try {

            new DownloadImageTask((ImageView) view.findViewById(R.id.badge))
                    .execute(point.getImageURL());

            String title = point.getName();
            getDialog().setTitle(title);

            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.WHITE), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = point.getDescription();
            String detail = point.getAudio();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            snippetUi.setText(snippet);

            TextView detailUi = ((TextView) view.findViewById(R.id.detail));
            detailUi.setText(detail);

//            if (snippet != null && snippet.length() > 12) {
//                SpannableString snippetText = new SpannableString(snippet);
//                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
//                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
//                snippetUi.setText(snippetText);
//            } else {
//                snippetUi.setText("");
//            }

            ImageButton playButton = (ImageButton)view.findViewById(R.id.btn_play);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onPlay();
                }
            });

            ImageButton speakButton = (ImageButton)view.findViewById(R.id.btn_ask);
            speakButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSpeech();
                }
            });


        }

        catch (Exception e) {

        }
        return view;
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
