package com.faisal.protoolkit.ui.tools.document;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.PointF;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.ActivityDocumentPageEditBinding;
import com.faisal.protoolkit.model.EditOps;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.util.RenderEngine;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

public class DocumentPageEditActivity extends AppCompatActivity {
    private ActivityDocumentPageEditBinding binding;
    private Bitmap originalBitmap;
    private PageEntity pageEntity;
    private AppDatabase database;
    private RenderEngine renderEngine;
    private EditOps currentEditOps;
    
    // Store the current scale and center to maintain zoom state
    private float currentScale = 0f;
    private PointF currentCenter = null;
    private boolean isTopActionBarVisible = false;
    private boolean isFilterOptionsVisible = false;
    private boolean isAdjustmentsPanelVisible = false;
    
    // ActivityResultLauncher for crop image
    private ActivityResultLauncher<Intent> cropImageLauncher;
    
    // Animation duration in milliseconds
    private static final int ANIMATION_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentPageEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize database and render engine
        database = AppDatabase.getDatabase(this);
        renderEngine = new RenderEngine(this);

        // Get page ID from intent
        String pageId = getIntent().getStringExtra("page_id");
        if (pageId == null) {
            Toast.makeText(this, "Page ID is required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load page from database
        Thread loadPageThread = new Thread(() -> {
            pageEntity = database.pageDao().getPageById(pageId);
            if (pageEntity != null) {
                // Load the image
                File imageFile = new File(pageEntity.uri_original);
                if (imageFile.exists()) {
                    originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (originalBitmap != null) {
                        // Initialize edit operations - try to deserialize existing ones first
                        if (pageEntity.edit_ops_json != null && !pageEntity.edit_ops_json.isEmpty()) {
                            try {
                                currentEditOps = com.faisal.protoolkit.util.EditOpsUtil.deserialize(pageEntity.edit_ops_json);
                            } catch (Exception e) {
                                e.printStackTrace();
                                currentEditOps = new EditOps(); // Fallback to new instance
                            }
                        } else {
                            currentEditOps = new EditOps(); // Initialize new edit operations
                        }

                        // Ensure filter object is initialized
                        if (currentEditOps.filter == null) {
                            currentEditOps.filter = new EditOps.Filter();
                        }
                        // Ensure the filter mode is set to a default value (ORIGINAL)
                        if (currentEditOps.filter.mode == null) {
                            currentEditOps.filter.mode = "ORIGINAL";
                        }

                        runOnUiThread(() -> {
                            setupUI();
        loadInitialImage();
        setupAdjustmentsPanel(); // Initialize the adjustments panel
        // Only enable controls after both originalBitmap and currentEditOps are properly initialized
        if (originalBitmap != null && !originalBitmap.isRecycled() && currentEditOps != null) {
            enableUIControls();
        } else {
            Log.e("DocumentPageEdit", "Cannot enable UI controls - originalBitmap or currentEditOps not ready");
        }
                        });
                    }
                }
            }
        });
        loadPageThread.start();
        
        // Initialize the crop image launcher for uCrop
        cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Handle successful crop result
                    Uri croppedImageUri = UCrop.getOutput(result.getData());
                    Log.d("DocumentPageEdit", "Crop successful, result URI: " + croppedImageUri);
                    
                    if (croppedImageUri != null) {
                        try {
                            // Load the cropped image
                            Bitmap croppedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(croppedImageUri));
                            if (croppedBitmap != null) {
                                // Update the original bitmap with the cropped version
                                if (originalBitmap != null && !originalBitmap.isRecycled()) {
                                    originalBitmap.recycle();
                                }
                                originalBitmap = croppedBitmap;
                                
                                // Update the preview with the cropped image
                                binding.imageViewPreview.setImage(ImageSource.bitmap(originalBitmap.copy(originalBitmap.getConfig(), true)));
                                
                                // Apply current filters to the cropped image
                                applyFilters();
                                
                                Toast.makeText(this, "Image cropped successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("DocumentPageEdit", "Error loading cropped image: " + e.getMessage());
                            Toast.makeText(this, "Error loading cropped image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    // Handle crop error
                    Throwable cropError = UCrop.getError(result.getData());
                    Log.e("DocumentPageEdit", "Crop error: " + cropError.getMessage());
                    Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    private void setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Page");
        }

        // Setup filter buttons - initially disabled until image is loaded
        binding.btnFilterOriginal.setEnabled(false);
        binding.btnFilterGray.setEnabled(false);
        binding.btnFilterBw.setEnabled(false);
        binding.btnFilterBoost.setEnabled(false);

        // Set up click listeners for filter image buttons
        binding.btnFilterOriginal.setOnClickListener(v -> {
            setFilterMode("ORIGINAL");
            updateFilterSelection("ORIGINAL");
            // Keep filter options visible after selection
        });
        binding.btnFilterGray.setOnClickListener(v -> {
            setFilterMode("GRAY");
            updateFilterSelection("GRAY");
            // Keep filter options visible after selection
        });
        binding.btnFilterBw.setOnClickListener(v -> {
            setFilterMode("BW");
            updateFilterSelection("BW");
            // Keep filter options visible after selection
        });
        binding.btnFilterBoost.setOnClickListener(v -> {
            setFilterMode("COLOR_BOOST");
            updateFilterSelection("COLOR_BOOST");
            // Keep filter options visible after selection
        });

        // Initially disable other controls too until image is loaded
        binding.seekbarContrast.setEnabled(false);
        binding.seekbarBrightness.setEnabled(false);
        binding.seekbarSharpen.setEnabled(false);
        
        // Setup rotation button (now in adjustments sheet)
        binding.btnRotateRight.setOnClickListener(v -> rotateRight());

        // Setup crop button (now in adjustments sheet)
        binding.btnCrop.setOnClickListener(v -> showCropOptions());

        // Setup "Action" button to show/hide adjustments panel
        binding.btnAction.setOnClickListener(v -> {
            Log.d("DocumentPageEdit", "Action button clicked");
            Log.d("DocumentPageEdit", "isAdjustmentsPanelVisible: " + isAdjustmentsPanelVisible);
            
            if (isAdjustmentsPanelVisible) {
                hideAdjustmentsSheet();
            } else {
                showAdjustmentsSheet();
            }
        });

        // Setup "Filter" button to show/hide filter options
        binding.btnFilter.setOnClickListener(v -> {
            showFilterOptions();
            hideTopActionBar(); // Hide the top bar when showing filters
        });

        // Setup save button
        binding.btnSave.setOnClickListener(v -> saveEdits());

        // Setup reset button
        binding.btnReset.setOnClickListener(v -> {
            resetAdjustments();
            hideAdjustmentsSheet();
        });

        // Setup contrast control
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.contrast = 1.0f;
        }
        
        binding.seekbarContrast.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                float contrast = value;
                Log.d("DocumentPageEdit", "Contrast changed to: " + contrast);
                updateContrast(contrast);
                binding.contrastValue.setText(String.format("%.1f", contrast));
            }
        });

        // Setup brightness control
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.brightness = 0.0f;
        }
        
        binding.seekbarBrightness.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                float brightness = value;
                Log.d("DocumentPageEdit", "Brightness changed to: " + brightness);
                updateBrightness(brightness);
                binding.brightnessValue.setText(String.format("%.1f", brightness));
            }
        });

        // Setup sharpen control
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.sharpen = 0.0f;
        }
        
        binding.seekbarSharpen.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                float sharpen = value;
                Log.d("DocumentPageEdit", "Sharpen changed to: " + sharpen);
                updateSharpen(sharpen);
                binding.sharpenValue.setText(String.format("%.1f", sharpen));
            }
        });

        // Setup touch listener for the image view to handle tap and long press
        binding.imageViewPreview.setOnTouchListener(new View.OnTouchListener() {
            private static final int LONG_PRESS_TIMEOUT = 500; // milliseconds
            private boolean isLongPressDetected = false;
            private android.os.Handler handler = new android.os.Handler();
            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    isLongPressDetected = true;
                    showTopActionBar();
                }
            };
            
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        isLongPressDetected = false;
                        handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                        handler.removeCallbacks(longPressRunnable);
                        if (!isLongPressDetected) {
                            // Short tap - toggle the top action bar
                            toggleTopActionBar();
                        }
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        // If user moves finger significantly, cancel long press
                        break;
                    case android.view.MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        break;
                }
                return false; // Return false to allow the SubsamplingScaleImageView to handle touch events
            }
        });
        
        // The touch listener above now handles both tap and long press
        // We don't need a separate click listener
        
        // Initialize and setup bottom sheet behavior
        setupAdjustmentsPanel();
    }
    
    private void setupAdjustmentsPanel() {
        // Initialize adjustments panel - similar to filter options panel
        if (binding.adjustmentsSheet.getVisibility() == View.GONE) {
            binding.adjustmentsSheet.setVisibility(View.GONE);
        }
        isAdjustmentsPanelVisible = false;
    }

    private void enableUIControls() {
        // Enable all UI controls after the image has been loaded
        runOnUiThread(() -> {
            binding.btnFilterOriginal.setEnabled(true);
            binding.btnFilterGray.setEnabled(true);
            binding.btnFilterBw.setEnabled(true);
            binding.btnFilterBoost.setEnabled(true);
            binding.seekbarContrast.setEnabled(true);
            binding.seekbarBrightness.setEnabled(true);
            binding.seekbarSharpen.setEnabled(true);
            binding.btnRotateRight.setEnabled(true);
            binding.btnCrop.setEnabled(true);
            binding.btnAction.setEnabled(true);

            Log.d("DocumentPageEdit", "enableUIControls: Setting up initial slider values");
            Log.d("DocumentPageEdit", "adjustmentsSheet visibility: " + binding.adjustmentsSheet.getVisibility());
            Log.d("DocumentPageEdit", "adjustmentsSheet view is null: " + (binding.adjustmentsSheet == null));

            // Set the current filter mode on the UI if it exists
            if (currentEditOps != null && currentEditOps.filter != null) {
                String mode = currentEditOps.filter.mode;
                updateFilterButtonSelection(mode);
                
                // Initialize slider values from the current edit ops
                binding.seekbarContrast.setValue(currentEditOps.filter.contrast);
                binding.contrastValue.setText(String.format("%.1f", currentEditOps.filter.contrast));
                
                binding.seekbarBrightness.setValue(currentEditOps.filter.brightness);
                binding.brightnessValue.setText(String.format("%.1f", currentEditOps.filter.brightness));
                
                binding.seekbarSharpen.setValue(currentEditOps.filter.sharpen);
                binding.sharpenValue.setText(String.format("%.1f", currentEditOps.filter.sharpen));
            }
        });
    }

    private void updateFilterButtonSelection(String mode) {
        // Update which filter button appears selected based on current mode
        Log.d("DocumentPageEdit", "Updating filter button selection for mode: " + mode);
        binding.btnFilterOriginal.setAlpha(mode != null && mode.equals("ORIGINAL") ? 1.0f : 0.5f);
        binding.btnFilterGray.setAlpha(mode != null && mode.equals("GRAY") ? 1.0f : 0.5f);
        binding.btnFilterBw.setAlpha(mode != null && mode.equals("BW") ? 1.0f : 0.5f);
        binding.btnFilterBoost.setAlpha(mode != null && mode.equals("COLOR_BOOST") ? 1.0f : 0.5f);
    }
    
    private void toggleTopActionBar() {
        if (isTopActionBarVisible) {
            hideTopActionBar();
        } else {
            showTopActionBar();
        }
    }
    
    private void showTopActionBar() {
        Log.d("DocumentPageEdit", "showTopActionBar called");
        Log.d("DocumentPageEdit", "Current visibility: " + binding.topActionBar.getVisibility());
        Log.d("DocumentPageEdit", "Current translationY: " + binding.topActionBar.getTranslationY());
        
        if (binding.topActionBar.getVisibility() == View.GONE) {
            binding.topActionBar.setVisibility(View.VISIBLE);
            binding.topActionBar.setAlpha(0f);
            
            // Use post to ensure the view is measured before getting its height
            binding.topActionBar.post(() -> {
                Log.d("DocumentPageEdit", "View height: " + binding.topActionBar.getHeight());
                
                // Set initial position above the screen
                binding.topActionBar.setTranslationY(-binding.topActionBar.getHeight());
                
                // Create animator to slide in from top
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(binding.topActionBar, "translationY", 0);
                slideIn.setDuration(ANIMATION_DURATION);
                
                // Create animator to fade in
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.topActionBar, "alpha", 0f, 1f);
                fadeIn.setDuration(ANIMATION_DURATION);
                
                // Combine animations
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(slideIn, fadeIn);
                
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        binding.topActionBar.setVisibility(View.VISIBLE);
                    }
                    
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isTopActionBarVisible = true;
                        Log.d("DocumentPageEdit", "Top action bar shown");
                    }
                });
                animatorSet.start();
            });
        } else {
            // If already visible, just make sure it's fully shown
            binding.topActionBar.setAlpha(1f);
            binding.topActionBar.setTranslationY(0);
            isTopActionBarVisible = true;
        }

        // Hide other panels when showing top bar
        if (isFilterOptionsVisible) {
            hideFilterOptions();
        }
        if (isAdjustmentsPanelVisible) {
            hideAdjustmentsSheet();
        }
    }
    
    private void hideTopActionBar() {
        if (binding.topActionBar.getVisibility() == View.VISIBLE) {
            // Create animator to slide out to top
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(binding.topActionBar, "translationY", 
                    -binding.topActionBar.getHeight());
            slideOut.setDuration(ANIMATION_DURATION);
            
            // Create animator to fade out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.topActionBar, "alpha", 1f, 0f);
            fadeOut.setDuration(ANIMATION_DURATION);
            
            // Combine animations
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideOut, fadeOut);
            
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.topActionBar.setVisibility(View.GONE);
                    isTopActionBarVisible = false;
                    Log.d("DocumentPageEdit", "Top action bar hidden");
                }
            });
            animatorSet.start();
        } else {
            isTopActionBarVisible = false;
        }
    }
    
    private void showFilterOptions() {
        Log.d("DocumentPageEdit", "showFilterOptions called");
        Log.d("DocumentPageEdit", "Current filter scroll view visibility: " + binding.filterScrollView.getVisibility());
        
        if (binding.filterScrollView.getVisibility() == View.GONE) {
            binding.filterScrollView.setVisibility(View.VISIBLE);
            binding.filterScrollView.setAlpha(0f);
            
            // Use post to ensure the view is measured before getting its height
            binding.filterScrollView.post(() -> {
                Log.d("DocumentPageEdit", "Filter scroll view height: " + binding.filterScrollView.getHeight());
                
                // Set initial position above the screen
                binding.filterScrollView.setTranslationY(-binding.filterScrollView.getHeight());
                
                // Create animator to slide in from top
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(binding.filterScrollView, "translationY", 0f);
                slideIn.setDuration(ANIMATION_DURATION);
                
                // Create animator to fade in
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.filterScrollView, "alpha", 0f, 1f);
                fadeIn.setDuration(ANIMATION_DURATION);
                
                // Combine animations
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(slideIn, fadeIn);
                
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        binding.filterScrollView.setVisibility(View.VISIBLE);
                    }
                    
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isFilterOptionsVisible = true;
                        Log.d("DocumentPageEdit", "Filter options shown");
                    }
                });
                animatorSet.start();
            });
        } else {
            // If already visible, just make sure it's fully shown
            binding.filterScrollView.setAlpha(1f);
            binding.filterScrollView.setTranslationY(0);
            isFilterOptionsVisible = true;
        }

        // Hide other panels when showing filter options
        if (isTopActionBarVisible) {
            hideTopActionBar();
        }
        if (isAdjustmentsPanelVisible) {
            hideAdjustmentsSheet();
        }
    }
    
    private void hideFilterOptions() {
        if (binding.filterScrollView.getVisibility() == View.VISIBLE) {
            // Create animator to slide out to top
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(binding.filterScrollView, "translationY", 
                    -binding.filterScrollView.getHeight());
            slideOut.setDuration(ANIMATION_DURATION);
            
            // Create animator to fade out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.filterScrollView, "alpha", 1f, 0f);
            fadeOut.setDuration(ANIMATION_DURATION);
            
            // Combine animations
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideOut, fadeOut);
            
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.filterScrollView.setVisibility(View.GONE);
                    isFilterOptionsVisible = false;
                    Log.d("DocumentPageEdit", "Filter options hidden");
                }
            });
            animatorSet.start();
        } else {
            isFilterOptionsVisible = false;
        }
    }
    
    private void showAdjustmentsSheet() {
        Log.d("DocumentPageEdit", "showAdjustmentsSheet called");
        Log.d("DocumentPageEdit", "adjustmentsSheet visibility: " + binding.adjustmentsSheet.getVisibility());
        
        // Hide other panels first
        if (isTopActionBarVisible) {
            hideTopActionBar();
        }
        if (isFilterOptionsVisible) {
            hideFilterOptions();
        }
        
        if (binding.adjustmentsSheet.getVisibility() == View.GONE) {
            binding.adjustmentsSheet.setVisibility(View.VISIBLE);
            binding.adjustmentsSheet.setAlpha(0f);
            binding.adjustmentsSheet.setTranslationY(-binding.adjustmentsSheet.getHeight());
            
            // Create animator to slide in from top
            ObjectAnimator slideIn = ObjectAnimator.ofFloat(binding.adjustmentsSheet, "translationY", 0);
            slideIn.setDuration(ANIMATION_DURATION);
            
            // Create animator to fade in
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.adjustmentsSheet, "alpha", 0f, 1f);
            fadeIn.setDuration(ANIMATION_DURATION);
            
            // Combine animations
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideIn, fadeIn);
            
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    binding.adjustmentsSheet.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    isAdjustmentsPanelVisible = true;
                    Log.d("DocumentPageEdit", "Adjustments sheet shown");
                }
            });
            animatorSet.start();
        } else {
            // If already visible, just make sure it's fully shown
            binding.adjustmentsSheet.setAlpha(1f);
            binding.adjustmentsSheet.setTranslationY(0);
            isAdjustmentsPanelVisible = true;
        }
    }
    
    private void hideAdjustmentsSheet() {
        if (binding.adjustmentsSheet.getVisibility() == View.VISIBLE) {
            // Create animator to slide out to top
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(binding.adjustmentsSheet, "translationY", 
                    -binding.adjustmentsSheet.getHeight());
            slideOut.setDuration(ANIMATION_DURATION);
            
            // Create animator to fade out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.adjustmentsSheet, "alpha", 1f, 0f);
            fadeOut.setDuration(ANIMATION_DURATION);
            
            // Combine animations
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideOut, fadeOut);
            
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.adjustmentsSheet.setVisibility(View.GONE);
                    isAdjustmentsPanelVisible = false;
                    Log.d("DocumentPageEdit", "Adjustments sheet hidden");
                }
            });
            animatorSet.start();
        } else {
            isAdjustmentsPanelVisible = false;
        }
    }
    
    private void updateFilterSelection(String selectedMode) {
        // Update the visual state of filter buttons to show which one is selected
        updateFilterButtonSelection(selectedMode);
    }
    
    private void setupFilterThumbnails() {
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            // Create thumbnails for each filter mode
            createFilterThumbnail("ORIGINAL", binding.btnFilterOriginal);
            createFilterThumbnail("GRAY", binding.btnFilterGray);
            createFilterThumbnail("BW", binding.btnFilterBw);
            createFilterThumbnail("COLOR_BOOST", binding.btnFilterBoost);
        }
    }
    
    private void createFilterThumbnail(String filterMode, ImageView imageView) {
        if (originalBitmap == null || originalBitmap.isRecycled()) return;
        
        // Create a copy of edit operations to apply the specific filter
        EditOps tempEditOps = new EditOps();
        if (currentEditOps != null) {
            // Copy the rotation and other operations
            tempEditOps.rotate = currentEditOps.rotate;
            
            // Ensure filter exists
            if (tempEditOps.filter == null) {
                tempEditOps.filter = new EditOps.Filter();
            }
            
            // Copy existing adjustments and apply the specific filter
            if (currentEditOps.filter != null) {
                tempEditOps.filter.contrast = currentEditOps.filter.contrast;
                tempEditOps.filter.brightness = currentEditOps.filter.brightness;
                tempEditOps.filter.sharpen = currentEditOps.filter.sharpen;
            } else {
                tempEditOps.filter.contrast = 1.0f;
                tempEditOps.filter.brightness = 0.0f;
                tempEditOps.filter.sharpen = 0.0f;
            }
        }
        
        // Set the specific filter mode for this thumbnail
        if (tempEditOps.filter == null) {
            tempEditOps.filter = new EditOps.Filter();
        }
        tempEditOps.filter.mode = filterMode;
        
        // Create a small version of the bitmap for the thumbnail
        Bitmap smallBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true);
        
        // Apply the filter to the small bitmap
        new Thread(() -> {
            try {
                Bitmap filteredBitmap = renderEngine.applyFilters(smallBitmap, tempEditOps);
                if (filteredBitmap != null) {
                    runOnUiThread(() -> {
                        imageView.setImageBitmap(filteredBitmap);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    imageView.setImageBitmap(smallBitmap);
                });
            }
        }).start();
    }
    
    private void resetAdjustments() {
        if (currentEditOps != null && currentEditOps.filter != null) {
            currentEditOps.filter.contrast = 1.0f;
            currentEditOps.filter.brightness = 0.0f;
            currentEditOps.filter.sharpen = 0.0f;
            
            // Update UI
            binding.seekbarContrast.setValue(1.0f);
            binding.contrastValue.setText("1.0");
            binding.seekbarBrightness.setValue(0.0f);
            binding.brightnessValue.setText("0.0");
            binding.seekbarSharpen.setValue(0.0f);
            binding.sharpenValue.setText("0.0");
            
            // Apply the reset filters
            applyFilters();
        }
    }

    private void loadInitialImage() {
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            // Create a copy for the image view to prevent the original from being recycled by the library
            Bitmap previewBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            if (previewBitmap != null) {
                binding.imageViewPreview.setImage(ImageSource.bitmap(previewBitmap));
                
                // Restore zoom state if previously saved
                if (currentScale > 0 && currentCenter != null) {
                    binding.imageViewPreview.setScaleAndCenter(currentScale, currentCenter);
                }
                
                Log.d("DocumentPageEdit", "Loaded initial image, applying filters");
                // Apply initial filters to show the current state
                applyFilters();
                
                // Generate filter thumbnails
                setupFilterThumbnails();
            } else {
                Log.e("DocumentPageEdit", "Failed to copy bitmap for initial image preview");
            }
        } else {
            Log.e("DocumentPageEdit", "loadInitialImage: originalBitmap is null or recycled");
        }
    }

    private void setFilterMode(String mode) {
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            String previousMode = currentEditOps.filter.mode;
            currentEditOps.filter.mode = mode;
            Log.d("DocumentPageEdit", "Setting filter mode from: " + previousMode + " to: " + mode);
            applyFilters();
            updateFilterButtonSelection(mode);
        } else {
            Log.e("DocumentPageEdit", "Cannot set filter mode - currentEditOps is null. Image may still be loading.");
            // Show a toast to inform the user
            Toast.makeText(this, "Please wait for image to load", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateContrast(float contrast) {
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.contrast = contrast;
            Log.d("chk", "applying filter");
            applyFilters();
        }
    }

    private void updateBrightness(float brightness) {
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.brightness = brightness;
            applyFilters();
        }
    }

    private void updateSharpen(float sharpen) {
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.sharpen = sharpen;
            applyFilters();
        }
    }

    private void rotateRight() {
        if (currentEditOps != null) {
            currentEditOps.rotate = (currentEditOps.rotate + 90) % 360;
            applyFilters();
        }
    }

    private void showCropOptions() {
        Log.d("DocumentPageEdit", "showCropOptions called");
        
        // Save the current image to a temporary file for cropping
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            try {
                // Create a temporary file to store the current image
                File tempDir = new File(getCacheDir(), "cropping");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                
                File tempFile = new File(tempDir, "temp_image_for_crop.jpg");
                
                // Compress and save the bitmap to the temporary file
                FileOutputStream out = new FileOutputStream(tempFile);
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
                
                // Create output file for cropped image
                File outputFile = new File(tempDir, "cropped_image.jpg");
                
                // Launch uCrop with the image
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                options.setCompressionQuality(90);
                options.setHideBottomControls(false);
                options.setFreeStyleCropEnabled(true);
                options.setShowCropGrid(true);
                options.setToolbarTitle("Crop Image");
                options.setToolbarColor(ContextCompat.getColor(this, R.color.md_theme_primary));
                options.setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_primary_dark));
                options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.md_theme_primary));
                
                UCrop.of(Uri.fromFile(tempFile), Uri.fromFile(outputFile))
                    .withOptions(options)
                    .start(this);
                        
            } catch (IOException e) {
                Log.e("DocumentPageEdit", "Error preparing image for crop: " + e.getMessage());
                Toast.makeText(this, "Error preparing image for crop: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("DocumentPageEdit", "Unexpected error preparing image for crop: " + e.getMessage());
                Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Image not available for cropping", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilters() {
        Log.d("DocumentPageEdit", "applyFilters called");
        if (originalBitmap != null && !originalBitmap.isRecycled() && currentEditOps != null) {
            Log.d("DocumentPageEdit", "Original bitmap: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
            Log.d("DocumentPageEdit", "Filter mode: " + (currentEditOps.filter != null ? currentEditOps.filter.mode : "null"));
            
            // Save current scale and center before applying filters
            SubsamplingScaleImageView imageView = binding.imageViewPreview;
            currentScale = imageView.getScale();
            currentCenter = imageView.getCenter();
            
            // Make a copy of the original bitmap to prevent it from being affected by recycling
            // This prevents the bitmap from being recycled during processing if activity is destroyed
            Bitmap originalBitmapCopy = originalBitmap.copy(originalBitmap.getConfig(), true);
            if (originalBitmapCopy == null) {
                Log.e("DocumentPageEdit", "Failed to copy original bitmap");
                return;
            }
            
            // Apply filters in background to avoid UI blocking
            new Thread(() -> {
                try {
                    Log.d("DocumentPageEdit", "Calling renderEngine.applyFilters with copied bitmap");
                    Bitmap processedBitmap = renderEngine.applyFilters(originalBitmapCopy, currentEditOps);
                    
                    if (processedBitmap != null) {
                        Log.d("DocumentPageEdit","Bitmap processed successfully: " + processedBitmap.getWidth() + "x" + processedBitmap.getHeight());
                        runOnUiThread(() -> {
                            // Set the new image while preserving the zoom state
                            binding.imageViewPreview.setImage(ImageSource.bitmap(processedBitmap));
                            if (currentScale > 0 && currentCenter != null) {
                                binding.imageViewPreview.setScaleAndCenter(currentScale, currentCenter);
                            }
                            Log.d("DocumentPageEdit", "Updated preview with processed bitmap");
                            // The SubsamplingScaleImageView handles bitmap lifecycle internally
                        });
                    }
                    else {
                        Log.e("DocumentPageEdit","Processed bitmap is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error applying filters: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
                } finally {
                    // Recycle the copied bitmap after processing is done
                    if (originalBitmapCopy != null && !originalBitmapCopy.isRecycled()) {
                        originalBitmapCopy.recycle();
                        Log.d("DocumentPageEdit", "Recycled originalBitmapCopy after processing");
                    }
                }
            }).start();
        } else {
            Log.e("DocumentPageEdit", "applyFilters: Original bitmap is null or recycled or currentEditOps is null");
            if (originalBitmap == null) {
                Log.e("DocumentPageEdit", "Original bitmap is null");
            } else if (originalBitmap.isRecycled()) {
                Log.e("DocumentPageEdit", "Original bitmap is recycled");
            }
            if (currentEditOps == null) {
                Log.e("DocumentPageEdit", "currentEditOps is null");
            }
        }
    }

private void saveEdits() {
    if (pageEntity != null && currentEditOps != null) {
        // Show saving progress
        runOnUiThread(() -> {
            Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();
        });

        // Serialize edit operations
        String editOpsJson = com.faisal.protoolkit.util.EditOpsUtil.serialize(currentEditOps);
        Log.d("DocumentPageEdit", "Saving page with ID: " + pageEntity.id);
        Log.d("DocumentPageEdit", "Edit ops JSON: " + editOpsJson);

        // Update the page in database
        pageEntity.edit_ops_json = editOpsJson;
        pageEntity.updated_at = System.currentTimeMillis();

        new Thread(() -> {
            try {
                database.pageDao().updatePage(pageEntity);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving changes: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}

@Override
public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle the result from uCrop
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                Uri resultUri = UCrop.getOutput(data);
                Log.d("DocumentPageEdit", "Crop successful, result URI: " + resultUri);
                
                if (resultUri != null) {
                    try {
                        // Load the cropped image
                        Bitmap croppedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        if (croppedBitmap != null) {
                            // Update the original bitmap with the cropped version
                            if (originalBitmap != null && !originalBitmap.isRecycled()) {
                                originalBitmap.recycle();
                            }
                            originalBitmap = croppedBitmap;
                            
                            // Update the preview with the cropped image
                            binding.imageViewPreview.setImage(ImageSource.bitmap(originalBitmap.copy(originalBitmap.getConfig(), true)));
                            
                            // Apply current filters to the cropped image
                            applyFilters();
                            
                            Toast.makeText(this, "Image cropped successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("DocumentPageEdit", "Error loading cropped image: " + e.getMessage());
                        Toast.makeText(this, "Error loading cropped image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Crop result URI is null", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Throwable cropError = UCrop.getError(data);
                Log.e("DocumentPageEdit", "Crop error: " + cropError.getMessage());
                Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Recycle bitmaps to free memory
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            Log.d("DocumentPageEdit", "Recycled originalBitmap in onDestroy");
        } else {
            Log.d("DocumentPageEdit", "originalBitmap was already recycled or null in onDestroy");
        }
        // Note: We don't keep references to processed bitmaps, so no need to recycle them
        binding = null;
    }
}