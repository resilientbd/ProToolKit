package com.faisal.protoolkit.ui.tools.document;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.slider.Slider;

import android.graphics.PointF;
import android.widget.ImageView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.ActivityDocumentPageEditBinding;
import com.faisal.protoolkit.model.EditOps;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.util.RenderEngine;

import java.io.File;

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
    private boolean isAdjustmentsVisible = false;
    
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
        
        // Setup rotation button (icon only)
        binding.btnRotateRight.setOnClickListener(v -> rotateRight());

        // Setup crop button (icon only)
        binding.btnCrop.setOnClickListener(v -> showCropOptions());

        // Setup "Action" button to show/hide adjustments sheet
        binding.btnAction.setOnClickListener(v -> {
            Log.d("DocumentPageEdit", "Action button clicked - starting action");
            Log.d("DocumentPageEdit", "isAdjustmentsVisible before: " + isAdjustmentsVisible);
            Log.d("DocumentPageEdit", "adjustmentsSheet visibility before: " + binding.adjustmentsSheet.getVisibility());
            
            showAdjustmentsSheet();
            hideTopActionBar(); // Hide the top bar when showing adjustments
            hideFilterOptions(); // Also hide filter options when showing adjustments
            
            Log.d("DocumentPageEdit", "Called showAdjustmentsSheet and hide methods");
            Log.d("DocumentPageEdit", "isAdjustmentsVisible after: " + isAdjustmentsVisible);
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

        // Setup tap listener for the image view to toggle top action bar
        binding.imageViewPreview.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                toggleTopActionBar();
            }
            return false; // Return false to allow the SubsamplingScaleImageView to handle touch events
        });
        
        // Add click listener to image to hide all panels when clicked outside
        binding.imageViewPreview.setOnClickListener(v -> {
            if (isTopActionBarVisible) {
                hideTopActionBar();
            }
            if (isFilterOptionsVisible) {
                hideFilterOptions();
            }
            if (isAdjustmentsVisible) {
                hideAdjustmentsSheet();
            }
        });
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
        if (binding.topActionBar.getVisibility() == View.GONE) {
            binding.topActionBar.setVisibility(View.VISIBLE);
            binding.topActionBar.setTranslationY(-binding.topActionBar.getHeight());
            
            ObjectAnimator animator = ObjectAnimator.ofFloat(binding.topActionBar, "translationY", 0);
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    binding.topActionBar.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
        }
        isTopActionBarVisible = true;

        // Hide other panels when showing top bar
        if (isFilterOptionsVisible) {
            hideFilterOptions();
        }
        if (isAdjustmentsVisible) {
            hideAdjustmentsSheet();
        }
    }
    
    private void hideTopActionBar() {
        if (binding.topActionBar.getVisibility() == View.VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(binding.topActionBar, "translationY", 
                    -binding.topActionBar.getHeight());
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.topActionBar.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
        isTopActionBarVisible = false;
    }
    
    private void showFilterOptions() {
        if (binding.filterScrollView.getVisibility() == View.GONE) {
            binding.filterScrollView.setVisibility(View.VISIBLE);
            binding.filterScrollView.setTranslationY(-binding.filterScrollView.getHeight());
            
            ObjectAnimator animator = ObjectAnimator.ofFloat(binding.filterScrollView, "translationY", 0f);
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    binding.filterScrollView.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
        }
        isFilterOptionsVisible = true;
    }
    
    private void hideFilterOptions() {
        if (binding.filterScrollView.getVisibility() == View.VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(binding.filterScrollView, "translationY", 
                    -binding.filterScrollView.getHeight());
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.filterScrollView.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
        isFilterOptionsVisible = false;
    }
    
    private void showAdjustmentsSheet() {
        Log.d("DocumentPageEdit", "showAdjustmentsSheet called");
        Log.d("DocumentPageEdit", "adjustmentsSheet visibility: " + binding.adjustmentsSheet.getVisibility());
        
        if (binding.adjustmentsSheet.getVisibility() == View.GONE) {
            Log.d("DocumentPageEdit", "Showing adjustments sheet - initial state is GONE");
            
            // Set the state immediately
            isAdjustmentsVisible = true;
            
            // Make sure it's visible first
            binding.adjustmentsSheet.setVisibility(View.VISIBLE);
            binding.adjustmentsSheet.setAlpha(0f); // Start transparent
            
            // Use a simple fade-in animation instead of translation
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.adjustmentsSheet, "alpha", 0f, 1f);
            fadeIn.setDuration(ANIMATION_DURATION);
            fadeIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Log.d("DocumentPageEdit", "Adjustments sheet fade-in started");
                    binding.adjustmentsSheet.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d("DocumentPageEdit", "Adjustments sheet fade-in ended");
                    // State is already set above
                }
            });
            
            fadeIn.start();
            Log.d("DocumentPageEdit", "Started fade-in animation to show adjustments sheet");
        } else {
            Log.d("DocumentPageEdit", "Adjustments sheet already visible");
            isAdjustmentsVisible = true;
        }
    }
    
    private void hideAdjustmentsSheet() {
        if (binding.adjustmentsSheet.getVisibility() == View.VISIBLE) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.adjustmentsSheet, "alpha", 1f, 0f);
            fadeOut.setDuration(ANIMATION_DURATION);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.adjustmentsSheet.setVisibility(View.GONE);
                    isAdjustmentsVisible = false;
                }
            });
            fadeOut.start();
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
        // For now, show a toast with "Coming Soon" message
        Toast.makeText(this, "Crop functionality coming soon", Toast.LENGTH_SHORT).show();
        // In the future, implement actual crop dialog with crop rectangle overlay
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