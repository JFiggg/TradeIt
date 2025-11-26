package edu.uga.cs.tradeit.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.objects.Category;
import edu.uga.cs.tradeit.objects.Item;

public class ItemDialogFragment extends DialogFragment {
    private EditText itemNameEditText;
    private EditText itemPriceEditText;

    private TextView categoryNameTextView;

    private CheckBox isFreeCheckBox;

    private Category selectedCategory;
    private Item itemToEdit = null;

    public interface ItemDialogListener {
        void addItem(Item item);
        void updateItem(Item item);
    }

    public void setCategory(Category category) {
        this.selectedCategory = category;

        // Only set text if view is already created
        if (categoryNameTextView != null) {
            categoryNameTextView.setText(category.getName());
        }
    }

    public void setItemToEdit(Item item) {
        this.itemToEdit = item;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.fragment_add_item_dialog, null);

        itemNameEditText = layout.findViewById(R.id.itemNameEditText);
        itemPriceEditText = layout.findViewById(R.id.priceEditText);
        isFreeCheckBox = layout.findViewById(R.id.freeCheckBox);

        categoryNameTextView = layout.findViewById(R.id.selectedCategoryTextView);

        // Check if we're editing an existing item
        boolean isEditMode = (itemToEdit != null);

        if (isEditMode) {
            // Pre-populate fields with existing item data
            itemNameEditText.setText(itemToEdit.getName());
            if (itemToEdit.isFree()) {
                isFreeCheckBox.setChecked(true);
                itemPriceEditText.setEnabled(false);
            } else {
                isFreeCheckBox.setChecked(false);
                itemPriceEditText.setText(String.valueOf(itemToEdit.getPrice()));
            }
            // Show category name (cannot be changed)
            categoryNameTextView.setText(itemToEdit.getCategoryName());
        } else {
            // Set category name if already set (for add mode)
            if (selectedCategory != null && categoryNameTextView != null) {
                categoryNameTextView.setText(selectedCategory.getName());
            }
        }

        // Add listener to enable/disable price field based on free checkbox
        isFreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Item is free - disable and clear price field
                itemPriceEditText.setEnabled(false);
                itemPriceEditText.setText("");
            } else {
                // Item is not free - enable price field
                itemPriceEditText.setEnabled(true);
            }
        });

        // create a new AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set its view (inflated above).
        builder.setView(layout);

        // Set title based on mode
        builder.setTitle(isEditMode ? "Edit item" : "Add item");

        // Provide the negative button listener
        builder.setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // close the dialog
                dialog.dismiss();
            }
        });

        // Provide the positive button listener
        builder.setPositiveButton( android.R.string.ok, new ItemDialogFragment.SaveItemListener() );

        // Create the AlertDialog and show it
        return builder.create();
    }

    private class SaveItemListener implements  DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String itemName = itemNameEditText.getText().toString();

            // Get free checkbox value
            boolean isFree = isFreeCheckBox.isChecked();

            // Get price based on whether item is free
            Double itemPrice = null;
            if (isFree) {
                itemPrice = null;  // Free items have no price
            } else {
                String priceText = itemPriceEditText.getText().toString().trim();
                if (!priceText.isEmpty()) {
                    try {
                        itemPrice = Double.parseDouble(priceText);
                    } catch (NumberFormatException e) {
                        itemPrice = 0.0;
                    }
                } else {
                    itemPrice = 0.0;
                }
            }

            // Get the listener from the parent fragment instead of activity
            ItemDialogListener listener = (ItemDialogListener) getParentFragment();

            if (itemToEdit != null) {
                // Edit mode - update existing item
                itemToEdit.setName(itemName);
                itemToEdit.setPrice(itemPrice);
                itemToEdit.setFree(isFree);
                // Category and createdAt are NOT changed
                listener.updateItem(itemToEdit);
            } else {
                // Add mode - create new item
                Item item = new Item(itemName, itemPrice, isFree, selectedCategory.getKey());
                listener.addItem(item);
            }

            dismiss();
        }
    }

}