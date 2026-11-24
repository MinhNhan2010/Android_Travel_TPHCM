package com.example.nhom_15_dulichtphcm.ui.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.AiChatMessage;
import com.example.nhom_15_dulichtphcm.data.model.AiItineraryDay;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MarkdownFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pure Java RecyclerView.Adapter rendering Smart Travel Guide chat messages from item_chat_message.xml.
 */
public final class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.VH> {

    private final List<AiChatMessage> items = new ArrayList<>();
    private final AppNavigator navigator;
    private final Consumer<String> onFollowUpClicked;

    public ChatMessageAdapter(AppNavigator navigator, Consumer<String> onFollowUpClicked) {
        this.navigator = navigator;
        this.onFollowUpClicked = onFollowUpClicked;
    }

    public void submitList(List<AiChatMessage> newList) {
        this.items.clear();
        if (newList != null) this.items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AiChatMessage msg = items.get(position);
        float density = h.itemView.getResources().getDisplayMetrics().density;

        h.rootRow.setGravity(msg.isUser ? Gravity.END : Gravity.START);
        h.avatarBot.setVisibility(msg.isUser ? View.GONE : View.VISIBLE);
        h.bubble.setBackgroundResource(msg.isUser ? R.drawable.bg_chat_user : R.drawable.bg_chat_bot);
        h.tvText.setTextColor(msg.isUser
                ? Color.WHITE
                : ContextCompat.getColor(h.itemView.getContext(), R.color.text_primary));
        h.tvText.setText(MarkdownFormatter.format(msg.text));

        h.containerExtras.removeAllViews();
        boolean hasExtras = false;

        for (AiItineraryDay day : msg.itineraryDays) {
            hasExtras = true;
            TextView tvDay = new TextView(h.itemView.getContext());
            tvDay.setBackgroundResource(R.drawable.bg_lavender_card);
            int pad = (int) (10 * density);
            tvDay.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (6 * density);
            tvDay.setLayoutParams(lp);
            tvDay.setTextSize(12.5f);
            tvDay.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.text_primary));
            String content = "**" + day.dayTitle + "**\n🌅 " + day.morningPlan
                    + "\n☀️ " + day.afternoonPlan + "\n🌆 " + day.eveningPlan;
            tvDay.setText(MarkdownFormatter.format(content));
            h.containerExtras.addView(tvDay);
        }

        for (Attraction a : msg.recommendedAttractions) {
            hasExtras = true;
            TextView btnAttr = new TextView(h.itemView.getContext());
            btnAttr.setBackgroundResource(R.drawable.bg_peach_soft_pill);
            int padH = (int) (12 * density);
            int padV = (int) (8 * density);
            btnAttr.setPadding(padH, padV, padH, padV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (6 * density);
            btnAttr.setLayoutParams(lp);
            btnAttr.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.saigon_red_dark));
            btnAttr.setTypeface(null, Typeface.BOLD);
            btnAttr.setTextSize(12.5f);
            btnAttr.setText("🏛️ " + Localized.tr(a.nameVi, a.nameEn) + " (" + a.province + ") →");
            btnAttr.setOnClickListener(v -> {
                if (navigator != null) navigator.openAttractionDetail(a.id);
            });
            h.containerExtras.addView(btnAttr);
        }

        for (SpecialtyFood f : msg.recommendedFoods) {
            hasExtras = true;
            TextView btnFood = new TextView(h.itemView.getContext());
            btnFood.setBackgroundResource(R.drawable.bg_teal_mint_pill);
            int padH = (int) (12 * density);
            int padV = (int) (8 * density);
            btnFood.setPadding(padH, padV, padH, padV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (6 * density);
            btnFood.setLayoutParams(lp);
            btnFood.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.saigon_teal_dark));
            btnFood.setTypeface(null, Typeface.BOLD);
            btnFood.setTextSize(12.5f);
            btnFood.setText("🍜 " + Localized.tr(f.nameVi, f.nameEn) + " →");
            btnFood.setOnClickListener(v -> {
                if (navigator != null) navigator.openFoodDetail(f.id);
            });
            h.containerExtras.addView(btnFood);
        }

        h.containerExtras.setVisibility(hasExtras ? View.VISIBLE : View.GONE);

        h.containerFollowUps.removeAllViews();
        if (!msg.isUser && !msg.quickFollowUps.isEmpty()) {
            h.tvFollowUpLabel.setVisibility(View.VISIBLE);
            h.scrollFollowUps.setVisibility(View.VISIBLE);
            for (String chipText : msg.quickFollowUps) {
                TextView chip = new TextView(h.itemView.getContext());
                chip.setBackgroundResource(R.drawable.bg_peach_soft_pill);
                int ph = (int) (12 * density);
                int pv = (int) (7 * density);
                chip.setPadding(ph, pv, ph, pv);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd((int) (6 * density));
                chip.setLayoutParams(lp);
                chip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.saigon_red));
                chip.setTypeface(null, Typeface.BOLD);
                chip.setTextSize(12f);
                chip.setText(chipText);
                chip.setOnClickListener(v -> {
                    if (onFollowUpClicked != null) onFollowUpClicked.accept(chipText);
                });
                h.containerFollowUps.addView(chip);
            }
        } else {
            h.tvFollowUpLabel.setVisibility(View.GONE);
            h.scrollFollowUps.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final LinearLayout rootRow;
        final TextView avatarBot;
        final LinearLayout bubble;
        final TextView tvText;
        final LinearLayout containerExtras;
        final TextView tvFollowUpLabel;
        final HorizontalScrollView scrollFollowUps;
        final LinearLayout containerFollowUps;

        VH(@NonNull View v) {
            super(v);
            rootRow = v.findViewById(R.id.root_chat_row);
            avatarBot = v.findViewById(R.id.avatar_chat_bot);
            bubble = v.findViewById(R.id.bubble_chat);
            tvText = v.findViewById(R.id.tv_chat_text);
            containerExtras = v.findViewById(R.id.container_chat_extras);
            tvFollowUpLabel = v.findViewById(R.id.tv_chat_followup_label);
            scrollFollowUps = v.findViewById(R.id.scroll_chat_followups);
            containerFollowUps = v.findViewById(R.id.container_chat_followups);
        }
    }
}
