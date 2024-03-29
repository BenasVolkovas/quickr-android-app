package com.benasvolkovas.quickr;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muddzdev.styleabletoast.StyleableToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// HabitsAdapter represents single task
public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout containerView;
        private TextView textView;
        private CheckBox checkBox;
        private TextView streakNum;
        public int id;
        public int isChecked;

        // Constructor
        public HabitViewHolder(View view) {
            super(view);
            this.containerView = view.findViewById(R.id.habit_row);
            this.textView = view.findViewById(R.id.habit_text);
            this.checkBox = view.findViewById(R.id.habit_checkbox);
            this.streakNum = view.findViewById(R.id.habit_streak);

            // When clicked opens Single Habit Activity and sends extras
            this.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Habit habit = (Habit) containerView.getTag();
                    Context context = v.getContext();
                    Intent intent = new Intent(v.getContext(), SingleHabitActivity.class);
                    intent.putExtra("id", habit.id);
                    intent.putExtra("content", habit.content);
                    intent.putExtra("streak", habit.streak);

                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                }
            });

            // When checkbox is clicked it adds streak
            this.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Gets habit tag, streak, checked info
                    Habit habit = (Habit) checkBox.getTag();
                    int streak = HabitsActivity.habitsDatabase.habitDao().getStreak(habit.id);
                    isChecked = HabitsActivity.habitsDatabase.habitDao().getCheckInfo(habit.id);

                    // If it was uncheck, makes it checked and vice versa
                    if (isChecked == 0) {
                        isChecked = 1;

                        // If scores DB is empty it creates one column
                        try {
                            Cursor cursor = MainActivity.scoresDatabase.query("SELECT * FROM scores", null);
                            if (cursor.getCount() == 0) {
                                MainActivity.scoresDatabase.scoreDao().create();
                                MainActivity.scoresDatabase.scoreDao().updatePoints((streak+1)*100);
                            } else {
                                MainActivity.scoresDatabase.scoreDao().updatePoints((streak+1)*100);
                            }
                        } catch (NullPointerException e) {
                            System.out.println(e.toString());
                        }

                        // Updates checked, streak info, shows toast
                        HabitsActivity.habitsDatabase.habitDao().isChecked(habit.id);
                        HabitsActivity.habitsDatabase.habitDao().updateStreak(habit.id);
                        StyleableToast.makeText(v.getContext(), "+" + (streak+1)*100 +" points", R.style.toast).show();
                    } else if (isChecked == 1) {
                        isChecked = 0;

                        // Removes points, streak and makes unchecked
                        MainActivity.scoresDatabase.scoreDao().removePoints(streak*100);
                        HabitsActivity.habitsDatabase.habitDao().removeStreak(habit.id);
                        HabitsActivity.habitsDatabase.habitDao().notChecked(habit.id);
                    }

                    // Gets current streak and sets text
                    streak = HabitsActivity.habitsDatabase.habitDao().getStreak(habit.id);
                    streakNum.setText(String.valueOf(streak));
                }
            });
        }
    }

    // Creates list for habits
    private List<Habit> habits = new ArrayList<>();

    // Gets current date
    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        String dayOfYear = String.valueOf(calendar.get(Calendar.DAY_OF_YEAR));

        return dayOfYear;
    }

    // Checks if habit was missed
    public boolean missedHabit(int id) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        int currentYear = calendar.get(Calendar.YEAR);
        String dayOfYear = getCurrentDate();
        int lastDay;

        if (HabitsActivity.habitsDatabase.habitDao().getLastupdate(id) == null) {
            HabitsActivity.habitsDatabase.habitDao().updateDate(dayOfYear, id);
            lastDay = Integer.parseInt(HabitsActivity.habitsDatabase.habitDao().getLastupdate(id));
        } else {
            lastDay = Integer.parseInt(HabitsActivity.habitsDatabase.habitDao().getLastupdate(id));
        }

        if (currentDay > lastDay) {
            return currentDay - lastDay > 1;
        } else if (currentDay < lastDay) {
            if (currentYear % 4 == 0) {
                int daysMissed = (366 - lastDay) + currentDay;
                if (daysMissed > 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                int daysMissed = (365 - lastDay) + currentDay;
                if (daysMissed > 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    // While view is creating it converts xml to java code
    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_row, parent, false);

        return new HabitViewHolder(view);
    }

    // When view is created
    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position) {
        // Gets current habit
        Habit current = habits.get(position);

        // Sets tags to current view
        holder.containerView.setTag(current);
        holder.checkBox.setTag(current);

        // Sets text to current info saved in database
        holder.textView.setText(current.content);
        holder.streakNum.setText(String.valueOf(current.streak));

        boolean missedHabit = missedHabit(current.id);
        String currentDate = getCurrentDate();
        String lastUpdate = HabitsActivity.habitsDatabase.habitDao().getLastupdate(current.id);

        // On create, if it was checked, makes checked, else unchecked
        if (currentDate.equals(lastUpdate)) {
            if (HabitsActivity.habitsDatabase.habitDao().getCheckInfo(current.id) == 1) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

        // If habit was missed it deletes streak
        } else if (missedHabit == true) {
            HabitsActivity.habitsDatabase.habitDao().updateDate(currentDate, current.id);
            HabitsActivity.habitsDatabase.habitDao().deleteStreak(current.id);
            HabitsActivity.habitsDatabase.habitDao().notChecked(current.id);
            holder.checkBox.setChecked(false);
            holder.streakNum.setText(String.valueOf(HabitsActivity.habitsDatabase.habitDao().getStreak(current.id)));
        } else {
            HabitsActivity.habitsDatabase.habitDao().updateDate(currentDate, current.id);
            HabitsActivity.habitsDatabase.habitDao().notChecked(current.id);
            holder.checkBox.setChecked(false);
        }
    }

    // Returns size of habits
    @Override
    public int getItemCount() {
        return habits.size();
    }

    // Gets all info from habits list and notifies that data has changed
    public void reload() {
        try {
            habits = HabitsActivity.habitsDatabase.habitDao().getAll();
        } catch (Exception error) {
            System.out.println("DB ERROR CATCH THAT PREVENTS CRASH");
            System.out.println(error.toString());
        }
        notifyDataSetChanged();
    }
}
