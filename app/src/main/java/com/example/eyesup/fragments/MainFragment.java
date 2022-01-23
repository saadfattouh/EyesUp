package com.example.eyesup.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesup.R;
import com.example.eyesup.adapters.NotesAdapter;
import com.example.eyesup.helper.SharedPrefManager;
import com.example.eyesup.sqlite.Myappdatabas;
import com.example.eyesup.sqlite.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class MainFragment extends Fragment {

    public static final String TAG = "mainFragment";


    NotesAdapter mAdapter;
    RecyclerView mNotesList;
    ArrayList<Notes> notes;

    FloatingActionButton mAddNoteBtn;


    Myappdatabas myappdatabas;

    Context ctx;

    int userId;

    public MainFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireContext();

        userId = SharedPrefManager.getInstance(ctx).getUserId();

        myappdatabas = Myappdatabas.getDatabase(ctx);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ctx = requireContext();

        mNotesList = view.findViewById(R.id.notes_list);
        mAddNoteBtn = view.findViewById(R.id.add_note);


        loadNotes();


        mAddNoteBtn.setOnClickListener(v -> {
            LayoutInflater factory = LayoutInflater.from(ctx);
            final View view1 = factory.inflate(R.layout.add_new_note_dialog, null);
            final AlertDialog addNewNoteDialog = new AlertDialog.Builder(ctx).create();
            addNewNoteDialog.setView(view1);

            EditText mNote = view1.findViewById(R.id.note);
            TextView yes = view1.findViewById(R.id.yes_btn);
            TextView no = view1.findViewById(R.id.no_btn);


            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String text = mNote.getText().toString();
                    if(TextUtils.isEmpty(text)){
                        Toast.makeText(ctx, "note details must not be empty", Toast.LENGTH_SHORT).show();
                    }else {
                        Notes note = new Notes();
                        note.setDone(false);
                        note.setUserId(userId);
                        note.setNote(text);
                        myappdatabas.myDao().addNote(note);
                        loadNotes();
                    }

                    //when done dismiss;
                    addNewNoteDialog.dismiss();

                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewNoteDialog.dismiss();
                }
            });
            addNewNoteDialog.show();
        });
    }

    private void loadNotes() {
        notes = (ArrayList<Notes>) myappdatabas.myDao().getUserNotes(userId);
        mAdapter = new NotesAdapter(notes, requireContext());
        mNotesList.setAdapter(mAdapter);
    }
}