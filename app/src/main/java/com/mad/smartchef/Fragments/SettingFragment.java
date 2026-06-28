package com.mad.smartchef.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.mad.smartchef.R;
import com.mad.smartchef.utils.LocalHelper;

public class SettingFragment extends Fragment {

    private Button btnEnglish, btnUrdu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btnEnglish = view.findViewById(R.id.btnEnglish);
        btnUrdu = view.findViewById(R.id.btnUrdu);

        btnEnglish.setOnClickListener(v -> {
            LocalHelper.saveLocale(requireContext(), "en");
            Toast.makeText(getActivity(), "English selected", Toast.LENGTH_SHORT).show();
            requireActivity().recreate();   // ✅ Force full refresh
        });

        btnUrdu.setOnClickListener(v -> {
            LocalHelper.saveLocale(requireContext(), "ur");
            Toast.makeText(getActivity(), "اردو منتخب کیا گیا", Toast.LENGTH_SHORT).show();
            requireActivity().recreate();   // ✅ Force full refresh
        });

        return view;
    }
}