package com.example.marvel_app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.marvel_app.ui.widget.ComicTransitionView;
public class MainActivity extends AppCompatActivity {
    private static final int[] DESTINATIONS = {
            R.id.nav_home, R.id.nav_explore, R.id.nav_duel, R.id.nav_jarvis, R.id.nav_archive
    };
    private static final int[] BUTTONS = {
            R.id.nav_button_home, R.id.nav_button_explore, R.id.nav_button_duel,
            R.id.nav_button_jarvis, R.id.nav_button_archive
    };
    private static final int[] ICONS = {
            R.id.nav_icon_home, R.id.nav_icon_explore, R.id.nav_icon_duel,
            R.id.nav_icon_jarvis, R.id.nav_icon_archive
    };
    private static final int[] LABELS = {
            R.id.nav_label_home, R.id.nav_label_explore, R.id.nav_label_duel,
            R.id.nav_label_jarvis, R.id.nav_label_archive
    };

    private ComicTransitionView tabTransition;
    private LinearLayout bottomNavigation;
    private NavController navController;
    private boolean bottomNavigationInteractionEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.content_container);
        if (host == null) {
            return;
        }
        navController = host.getNavController();
        bottomNavigation = findViewById(R.id.bottom_navigation);
        tabTransition = ComicTransitionView.attachTo(this);
        for (int index = 0; index < DESTINATIONS.length; index++) {
            final int destinationId = DESTINATIONS[index];
            View button = findViewById(BUTTONS[index]);
            TextView label = findViewById(LABELS[index]);
            button.setContentDescription(label.getText());
            button.setOnClickListener(view -> selectDestination(destinationId));
        }
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateBottomNavigation(destination);
        });
        updateBottomNavigation(navController.getCurrentDestination());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (navController != null) {
            updateBottomNavigation(navController.getCurrentDestination());
        }
    }

    private void updateBottomNavigation(NavDestination destination) {
        if (bottomNavigation == null || destination == null) return;
        int id = destination.getId();
        boolean topLevel = isTopLevel(id);
        bottomNavigation.setVisibility(topLevel ? View.VISIBLE : View.GONE);
        bottomNavigation.setAlpha(1f);
        View content = findViewById(R.id.content_container);
        ViewGroup.MarginLayoutParams contentParams =
                (ViewGroup.MarginLayoutParams) content.getLayoutParams();
        int bottomMargin = topLevel
                ? getResources().getDimensionPixelSize(R.dimen.bottom_navigation_height)
                : 0;
        if (contentParams.bottomMargin != bottomMargin) {
            contentParams.bottomMargin = bottomMargin;
            content.setLayoutParams(contentParams);
        }
        applyBottomNavigationInteractionState();
        if (topLevel) {
            int selectedColor = getColor(R.color.marvel_red);
            int normalColor = getColor(R.color.text_secondary);
            for (int index = 0; index < DESTINATIONS.length; index++) {
                boolean selected = DESTINATIONS[index] == id;
                ImageView icon = findViewById(ICONS[index]);
                TextView label = findViewById(LABELS[index]);
                View button = findViewById(BUTTONS[index]);
                icon.setColorFilter(selected ? selectedColor : normalColor);
                label.setTextColor(selected ? selectedColor : normalColor);
                label.setVisibility(selected ? View.VISIBLE : View.GONE);
                button.setSelected(selected);
            }
            bottomNavigation.bringToFront();
        }
    }

    private void selectDestination(int destinationId) {
        if (!bottomNavigationInteractionEnabled || navController == null) return;
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == destinationId) {
            if (destinationId == R.id.nav_home) navController.popBackStack(R.id.nav_home, false);
            return;
        }
        setBottomNavigationInteractionEnabled(false);
        tabTransition.play(destinationId, () -> {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                navigateToTopLevel(navController, destinationId);
                updateBottomNavigation(navController.getCurrentDestination());
            }
            setBottomNavigationInteractionEnabled(true);
        });
    }

    public void setBottomNavigationInteractionEnabled(boolean enabled) {
        bottomNavigationInteractionEnabled = enabled;
        applyBottomNavigationInteractionState();
    }

    private void applyBottomNavigationInteractionState() {
        if (bottomNavigation == null) return;
        for (int buttonId : BUTTONS) {
            View item = findViewById(buttonId);
            item.setClickable(bottomNavigationInteractionEnabled);
            item.setFocusable(bottomNavigationInteractionEnabled);
        }
    }

    private boolean navigateToTopLevel(NavController navController, int destinationId) {
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == destinationId) {
            return true;
        }

        if (destinationId == R.id.nav_home
                && navController.popBackStack(R.id.nav_home, false)) {
            return true;
        }

        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.nav_home, false, true)
                .build();
        try {
            navController.navigate(destinationId, null, options);
            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return false;
        }
    }

    private boolean isTopLevel(int destinationId) {
        return destinationId == R.id.nav_home
                || destinationId == R.id.nav_explore
                || destinationId == R.id.nav_duel
                || destinationId == R.id.nav_jarvis
                || destinationId == R.id.nav_archive;
    }

    @Override
    protected void onDestroy() {
        if (tabTransition != null) {
            tabTransition.cancel();
            tabTransition = null;
        }
        bottomNavigation = null;
        navController = null;
        super.onDestroy();
    }
}
