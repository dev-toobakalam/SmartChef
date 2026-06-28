package com.mad.smartchef.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.smartchef.R;
import com.mad.smartchef.data.AppDatabase;
import com.mad.smartchef.data.ShoppingItemEntity;

import java.util.List;

public class ShoppingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShoppingAdapter adapter;
    private EditText inputItem;
    private Button addButton;
    private TextView emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        recyclerView = view.findViewById(R.id.shoppingRecyclerView);
        inputItem = view.findViewById(R.id.shoppingInput);
        addButton = view.findViewById(R.id.addShoppingButton);
        emptyView = view.findViewById(R.id.emptyShoppingText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShoppingAdapter();
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            String name = inputItem.getText().toString().trim();
            if (!name.isEmpty()) {
                ShoppingItemEntity item = new ShoppingItemEntity();
                item.name = name;
                item.checked = false;
                new Thread(() -> {
                    AppDatabase.getInstance(requireContext()).shoppingDao().insert(item);
                    requireActivity().runOnUiThread(this::loadItems);
                }).start();
                inputItem.setText("");
            }
        });

        loadItems();
        return view;
    }

    private void loadItems() {
        new Thread(() -> {
            List<ShoppingItemEntity> items = AppDatabase.getInstance(requireContext())
                    .shoppingDao().getAll();
            requireActivity().runOnUiThread(() -> {
                adapter.setItems(items);
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {
        private List<ShoppingItemEntity> items;

        void setItems(List<ShoppingItemEntity> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shopping, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShoppingItemEntity item = items.get(position);
            holder.checkBox.setText(item.name);
            holder.checkBox.setChecked(item.checked);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.checked = isChecked;
                new Thread(() -> {
                    AppDatabase.getInstance(requireContext()).shoppingDao().update(item);
                }).start();
            });

            // SAFE DELETION – uses item ID and position, but removes by item reference
            holder.deleteButton.setOnClickListener(v -> {
                // Disable button to prevent multiple clicks
                holder.deleteButton.setEnabled(false);

                new Thread(() -> {
                    AppDatabase.getInstance(requireContext()).shoppingDao().delete(item.id);
                    requireActivity().runOnUiThread(() -> {
                        // Remove the item from the list using its ID
                        if (items != null) {
                            int index = -1;
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).id == item.id) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index != -1) {
                                items.remove(index);
                                notifyItemRemoved(index);
                                // Check if empty
                                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        }
                        // Re-enable button (though the view will be removed)
                    });
                }).start();
            });
        }

        @Override
        public int getItemCount() { return items == null ? 0 : items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            Button deleteButton;
            ViewHolder(View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.shoppingCheckbox);
                deleteButton = itemView.findViewById(R.id.shoppingDelete);
            }
        }
    }
}