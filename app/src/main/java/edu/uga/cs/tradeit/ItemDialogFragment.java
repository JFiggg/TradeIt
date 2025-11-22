package edu.uga.cs.tradeit;

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

public class ItemDialogFragment extends DialogFragment {
    private EditText itemNameEditText;
    private EditText itemPriceEditText;

    private TextView categoryNameTextView;

    private CheckBox isFreeCheckBox;

    private Category selectedCategory;
    private Item itemToEdit = null;

    public interface ItemDialogListener {
        void addItem(Item item);
//        void updateItem(Item item);
//        void deleteItem(Item item);
    }

    public void setCategory(Category category) {
        this.selectedCategory = category;

        // Only set text if view is already created
        if (categoryNameTextView != null) {
            categoryNameTextView.setText(category.getName());
        }
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

        // Set category name if already set
        if (selectedCategory != null && categoryNameTextView != null) {
            categoryNameTextView.setText(selectedCategory.getName());
        }

        // create a new AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set its view (inflated above).
        builder.setView(layout);

        builder.setTitle("Add item");

        // Provide the negative button listener
        builder.setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // close the dialog
                dialog.dismiss();
            }
        });

        // Provide the positive button listener
        builder.setPositiveButton( android.R.string.ok, new ItemDialogFragment.AddItemListener() );

        // Create the AlertDialog and show it
        return builder.create();
    }

    private class AddItemListener implements  DialogInterface.OnClickListener {

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

            Item item = new Item(itemName, itemPrice, isFree, selectedCategory.getKey());
            listener.addItem(item);

            dismiss();
        }
    }

}