package com.google.ar.core.codelab.function;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.ar.core.codelab.Activities.QuestionaireActivity;
import com.google.ar.core.codelab.depth.R;
import java.util.HashMap;
import java.util.List;

public class QuestionaireAdapter extends RecyclerView.Adapter<QuestionaireAdapter.ViewHolder> {

    private List<Questionnaire> questionnaires;
    private HashMap<Integer, Integer> checkedStates = new HashMap<>();
    private OnCategoryChangeListener categoryChangeListener;

    public interface OnCategoryChangeListener {
        void onCategoryChanged(HashMap<Integer, Integer> checkedStates);
    }

    public QuestionaireAdapter(List<Questionnaire> questionnaires, OnCategoryChangeListener listener) {
        this.questionnaires = questionnaires;
        this.categoryChangeListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_questionaire, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Questionnaire questionnaire = questionnaires.get(position);
        holder.questionnaireText.setText(questionnaire.getQuestion());
        holder.questionnaireDetails.setText(questionnaire.getDetails());
        holder.questionnaireIcon.setImageResource(questionnaire.getImageResource());
        holder.questionnaireCheckbox.setChecked(questionnaire.isChecked());

        holder.questionnaireCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            questionnaire.setChecked(isChecked);
//            if (isChecked) {
//                int category = determineCategory(position);
//                checkedStates.put(position, category);
//
//            } else {
//                checkedStates.remove(position);
//            }
            if (isChecked) {
                checkedStates.put(questionnaire.getCategory(),questionnaire.getCategory());
            } else {
                checkedStates.remove(questionnaire.getCategory());
            }
            categoryChangeListener.onCategoryChanged(new HashMap<>(checkedStates));
        });
    }

    @Override
    public int getItemCount() {
        return questionnaires.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView questionnaireIcon;
        TextView questionnaireText;
        TextView questionnaireDetails;
        CheckBox questionnaireCheckbox;

        public ViewHolder(View itemView) {
            super(itemView);
            questionnaireIcon = itemView.findViewById(R.id.questionaire_icon);
            questionnaireText = itemView.findViewById(R.id.questionaire_text);
            questionnaireDetails = itemView.findViewById(R.id.questionaire_details);
            questionnaireCheckbox = itemView.findViewById(R.id.questionaire_checkbox);
        }
    }

    private int determineCategory(int position) {
        if (position >= 0 && position <= 5) {
            return QuestionaireActivity.CATEGORY_5MM;
        } else if (position > 5 && position <= 10) {
            return QuestionaireActivity.CATEGORY_10MM;
        } else if (position > 10 && position <= 15) {
            return QuestionaireActivity.CATEGORY_15MM;
        }  else if (position == 16) {
            return QuestionaireActivity.CATEGORY_VACCINE;}
        else if (position == 17) {
            return QuestionaireActivity.CATEGORY_CONTACT;}
        else {
            return QuestionaireActivity.NO_ANSWER;
        }
    }

}
