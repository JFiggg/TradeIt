package edu.uga.cs.tradeit.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

import edu.uga.cs.tradeit.ProfileFragment;
import edu.uga.cs.tradeit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment {

    private static final String DEBUG_TAG = "SignInFragment";

    private FirebaseAuth mAuth;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public SignInFragment() {
        // Required empty public constructor
    }


    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add navigation button listener
        view.findViewById(R.id.signinNavButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, new RegisterFragment())
                        .commit();
            }
        });

        view.findViewById(R.id.finishSignInButton).setOnClickListener(new OnClickSignIn());
    }

    private class OnClickSignIn implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Get the password & email text fields
            EditText emailEditText = getView().findViewById(R.id.emailEditText);
            EditText passwordEditText = getView().findViewById(R.id.passwordEditText);


            signIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
        }
    }

    public void signIn(String email, String password) {

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(DEBUG_TAG, "sign in success");

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.container, new ProfileFragment())
                                    .commit();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(DEBUG_TAG, "sign in failure", task.getException());
                            Toast.makeText(getContext(), "Invalid email or password.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}