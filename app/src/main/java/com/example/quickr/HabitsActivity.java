package com.example.quickr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// HabitsActivity contains all habits in recycler view
public class HabitsActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private HabitsAdapter adapter;
    public static HabitsDatabase habitsDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        // Finds bottomNavigationView and sets listener
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_habits);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                // If item is clicked, it starts activity
                switch (menuItem.getItemId()) {
                    case R.id.action_tasks:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.action_habits:
                        return true;

                    case R.id.action_goals:
                        startActivity(new Intent(getApplicationContext(), GoalsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        // Builds habitsDatabase
        habitsDatabase = HabitsDatabase.getAppDatabase(getApplicationContext());
//        habitsDatabase = Room
//                .databaseBuilder(getApplicationContext(), HabitsDatabase.class, "habits")
//                .allowMainThreadQueries()
//                .build();

        recyclerView = findViewById(R.id.habits_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        adapter = new HabitsAdapter();

        // Joins everything to recycler view
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Creates button for adding new habits
        FloatingActionButton fab = findViewById(R.id.add_habit_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                habitsDatabase.habitDao().create();
                adapter.reload();
            }
        });
    }

    // When brought to foreground reloads data
    @Override
    protected void onResume() {
        super.onResume();

        adapter.reload();
    }
}
