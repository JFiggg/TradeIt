package edu.uga.cs.tradeit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CategoryDialogFragment extends DialogFragment {
    private EditText categoryNameEditText;
    private Category categoryToEdit = null;

    public interface AddCategoryDialogListener {
        void addCategory(Category category);
        void updateCategory(Category category);
        void deleteCategory(Category category);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.fragment_add_category_dialog, null);

        categoryNameEditText = layout.findViewById(R.id.categoryNameEditText);

        // create a new AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set its view (inflated above).
        builder.setView(layout);

        if (categoryToEdit != null) {
            // Edit mode
            builder.setTitle("Edit Category");
            categoryNameEditText.setText(categoryToEdit.getName());
        } else {
            // Add mode
            builder.setTitle("New Category");
        }

        // Provide the negative button listener
        builder.setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // close the dialog
                dialog.dismiss();
            }
        });

        // Provide the positive button listener
        builder.setPositiveButton( android.R.string.ok, new AddCategoryListener() );

        // Create the AlertDialog and show it
        return builder.create();
    }

    private class AddCategoryListener implements  DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String categoryName = categoryNameEditText.getText().toString();

            // Get the listener from the parent fragment instead of activity
            AddCategoryDialogListener listener = (AddCategoryDialogListener) getParentFragment();

            if (categoryToEdit == null) {
                // ADD MODE
                Category category = new Category(categoryName);
                listener.addCategory(category);
            } else {
                // UPDATE MODE
                categoryToEdit.setName(categoryName);
                listener.updateCategory(categoryToEdit);   // you'll add this to the interface
            }

            dismiss();
        }
    }

    public void setCategoryToEdit(Category category) {
        this.categoryToEdit = category;
    }
}
