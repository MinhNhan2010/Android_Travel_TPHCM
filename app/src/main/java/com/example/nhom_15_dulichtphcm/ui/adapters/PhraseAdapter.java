package com.example.nhom_15_dulichtphcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.DialogHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pure Java RecyclerView.Adapter binding survival Vietnamese phrases from item_phrase.xml (Old Style).
 */
public final class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.VH> {

    private final List<Phrase> items = new ArrayList<>();
    private final AppNavigator navigator;
    private final Consumer<Phrase> onToggleSaved;
    private final Consumer<Phrase> onDeleteCustom;

    public PhraseAdapter(
            AppNavigator navigator,
            Consumer<Phrase> onToggleSaved,
            Consumer<Phrase> onDeleteCustom
    ) {
        this.navigator = navigator;
        this.onToggleSaved = onToggleSaved;
        this.onDeleteCustom = onDeleteCustom;
    }

    public void submitList(List<Phrase> newList) {
        this.items.clear();
        if (newList != null) this.items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phrase, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Phrase p = items.get(position);
        String tag = p.category.emoji + " " + Localized.tr(p.category.displayNameVi, p.category.displayName)
                + (p.isCustom ? " • ✨ Custom" : "");
        h.tvCategory.setText(tag);
        h.tvEn.setText(p.english);
        h.tvVi.setText(p.vietnamese);
        if (h.tvUnaccented != null) {
            if (p.vietnameseUnaccented != null && !p.vietnameseUnaccented.isEmpty()) {
                h.tvUnaccented.setVisibility(View.VISIBLE);
                h.tvUnaccented.setText(p.vietnameseUnaccented);
            } else {
                h.tvUnaccented.setVisibility(View.GONE);
            }
        }
        h.tvPron.setText("Say: [ " + p.pronunciation + " ]");
        h.tvExp.setText(p.explanation);

        h.btnSave.setText(p.isSaved ? "❤️" : "🤍");
        h.btnSave.setOnClickListener(v -> {
            if (onToggleSaved != null) onToggleSaved.accept(p);
        });

        h.btnDeleteCustom.setVisibility(p.isCustom ? View.VISIBLE : View.GONE);
        h.btnDeleteCustom.setOnClickListener(v -> {
            if (onDeleteCustom != null) onDeleteCustom.accept(p);
        });

        SpeechHelper speech = navigator != null ? navigator.getSpeechHelper() : null;
        h.btnSpeakNormal.setText("🔊");
        h.btnSpeakNormal.setOnClickListener(v -> {
            if (speech != null) speech.speakVietnamese(p.vietnamese);
        });

        h.btnSpeakSlow.setText("🐢");
        h.btnSpeakSlow.setOnClickListener(v -> {
            if (speech != null) speech.speakVietnameseSlow(p.vietnamese);
        });

        h.btnShowCard.setText("📱");
        h.btnShowCard.setOnClickListener(v ->
                DialogHelper.showPhraseToLocalCard(v.getContext(), p, speech));
        h.itemView.setOnClickListener(v ->
                DialogHelper.showPhraseToLocalCard(v.getContext(), p, speech));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView tvCategory;
        final TextView btnDeleteCustom;
        final TextView btnSave;
        final TextView tvVi;
        final TextView tvUnaccented;
        final TextView tvEn;
        final TextView tvPron;
        final TextView tvExp;
        final TextView btnSpeakNormal;
        final TextView btnSpeakSlow;
        final TextView btnShowCard;

        VH(@NonNull View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_phrase_category);
            btnDeleteCustom = v.findViewById(R.id.btn_phrase_delete_custom);
            btnSave = v.findViewById(R.id.btn_phrase_save);
            tvVi = v.findViewById(R.id.tv_phrase_vi);
            tvUnaccented = v.findViewById(R.id.tv_phrase_unaccented);
            tvEn = v.findViewById(R.id.tv_phrase_en);
            tvPron = v.findViewById(R.id.tv_phrase_pronunciation);
            tvExp = v.findViewById(R.id.tv_phrase_explanation);
            btnSpeakNormal = v.findViewById(R.id.btn_phrase_speak_normal);
            btnSpeakSlow = v.findViewById(R.id.btn_phrase_speak_slow);
            btnShowCard = v.findViewById(R.id.btn_phrase_show_card);
        }
    }
}
