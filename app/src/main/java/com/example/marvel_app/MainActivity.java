package com.example.marvel_app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ComicTransitionView tabTransition;
    private BottomNavigationView bottomNavigation;
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
        bottomNavigation.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_SELECTED);
        bottomNavigation.setOnItemSelectedListener(item -> {
            NavDestination current = navController.getCurrentDestination();
            if (current != null && current.getId() == item.getItemId()) {
                return true;
            }
            tabTransition.play(() -> {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    if (!navigateToTopLevel(navController, item.getItemId())) {
                        restoreSelectedDestination(bottomNavigation, navController);
                    }
                } else {
                    restoreSelectedDestination(bottomNavigation, navController);
                }
            });
            return true;
        });
        bottomNavigation.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                navController.popBackStack(R.id.nav_home, false);
            }
        });
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
        for (int index = 0; index < bottomNavigation.getMenu().size(); index++) {
            bottomNavigation.getMenu().getItem(index).setEnabled(true);
        }
        applyBottomNavigationInteractionState();
        if (topLevel) {
            bottomNavigation.getMenu().findItem(id).setChecked(true);
            bottomNavigation.bringToFront();
        }
    }

    public void setBottomNavigationInteractionEnabled(boolean enabled) {
        bottomNavigationInteractionEnabled = enabled;
        applyBottomNavigationInteractionState();
    }

    private void applyBottomNavigationInteractionState() {
        if (bottomNavigation == null) return;
        for (int index = 0; index < bottomNavigation.getMenu().size(); index++) {
            View item = bottomNavigation.findViewById(
                    bottomNavigation.getMenu().getItem(index).getItemId());
            if (item != null) {
                item.setClickable(bottomNavigationInteractionEnabled);
                item.setFocusable(bottomNavigationInteractionEnabled);
            }
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

    private void restoreSelectedDestination(
            BottomNavigationView bottomNavigation,
            NavController navController
    ) {
        NavDestination destination = navController.getCurrentDestination();
        if (destination != null && isTopLevel(destination.getId())) {
            bottomNavigation.getMenu().findItem(destination.getId()).setChecked(true);
        }
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
