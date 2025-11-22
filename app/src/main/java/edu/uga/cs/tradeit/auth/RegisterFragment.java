package edu.uga.cs.tradeit.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uga.cs.tradeit.HomeActivity;
import edu.uga.cs.tradeit.ProfileFragment;
import edu.uga.cs.tradeit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {
    private static final String DEBUG_TAG = "RegisterFragment";
    private FirebaseAuth mAuth;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.signinNavButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, new SignInFragment())
                        .commit();
            }
        });

        view.findViewById(R.id.registerButton).setOnClickListener(new OnClickFinishRegistration());
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }

    private class OnClickFinishRegistration implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            EditText emailEditText = getView().findViewById(R.id.emailEditText);
            String emailText = emailEditText.getText().toString();
            boolean validEmail = validate(emailText);

            if (!validEmail) {
                Toast.makeText(getContext(), "Invalid email format",
                        Toast.LENGTH_SHORT).show();

                return;
            }


            // here show error if email malformed

            EditText passwordEditText = getView().findViewById(R.id.passwordEditText);

            EditText nameEditText = getView().findViewById((R.id.nameEditText));

            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                // Default username to User
                name = "User";
            }

            register(emailText, passwordEditText.getText().toString(), name);
        }
    }


    public void register(String email, String password, String username) {
        mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(getContext(),
                                    "Registered user: " + email,
                                    Toast.LENGTH_SHORT).show();

                            Log.d(DEBUG_TAG, "createUserWithEmail: success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            UserProfileChangeRequest updateProfile = new UserProfileChangeRequest.Builder().setDisplayName(username).build();

                            // Update their display name to the name we let them choose
                            user.updateProfile(updateProfile).addOnCompleteListener(job -> {
                                if (job.isSuccessful()) {

                                    Log.d(DEBUG_TAG, "Updated username");
                                } else {
                                    Log.d(DEBUG_TAG, "Failed to update username");
                                }

                                // Navigate to auth screen
                                Intent intent = new Intent(requireContext(), HomeActivity.class);
                                startActivity(intent);
                            });

                        } else {
                            // Show a specific error message if email is already taken
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getContext(), "This email is already in use!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(DEBUG_TAG, "createUserWithEmail: failure", task.getException());
                                Toast.makeText(getContext(), "Registration failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }
}