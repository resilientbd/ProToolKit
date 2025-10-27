package com.faisal.protoolkit.ui.tools.document;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
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

        // Setup quick filter buttons (icon only) - initially disabled until image is loaded
        binding.btnFilterOriginal.setEnabled(false);
        binding.btnFilterGray.setEnabled(false);
        binding.btnFilterBw.setEnabled(false);
        binding.btnFilterBoost.setEnabled(false);

        // Set up click listeners
        binding.btnFilterOriginal.setOnClickListener(v -> setFilterMode("ORIGINAL"));
        binding.btnFilterGray.setOnClickListener(v -> setFilterMode("GRAY"));
        binding.btnFilterBw.setOnClickListener(v -> setFilterMode("BW"));
        binding.btnFilterBoost.setOnClickListener(v -> setFilterMode("COLOR_BOOST"));

        // Initially disable other controls too until image is loaded
        binding.seekbarContrast.setEnabled(false);
        binding.seekbarBrightness.setEnabled(false);
        binding.seekbarSharpen.setEnabled(false);
        binding.btnRotateRight.setEnabled(false);
        binding.btnCrop.setEnabled(false);

        // Setup contrast control
        binding.seekbarContrast.setProgress(100); // Default value for contrast (1.0)
        // Initialize with default value
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.contrast = 1.0f;
        }
        binding.seekbarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float contrast = progress / 100.0f; // Convert 0-200 to 0.0-2.0
                    Log.d("DocumentPageEdit", "Contrast changed to: " + contrast);
                    updateContrast(contrast);
                    binding.contrastValue.setText(String.format("%.1f", contrast));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Setup brightness control
        binding.seekbarBrightness.setProgress(100); // Default value for brightness (0.0)
        // Initialize with default value
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.brightness = 0.0f;
        }
        binding.seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float brightness = (progress - 100) / 100.0f; // Convert 0-200 to -1.0 to 1.0
                    Log.d("DocumentPageEdit", "Brightness changed to: " + brightness);
                    updateBrightness(brightness);
                    binding.brightnessValue.setText(String.format("%.1f", brightness));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Setup sharpen control
        binding.seekbarSharpen.setProgress(0); // Default value for sharpen (0.0)
        // Initialize with default value
        if (currentEditOps != null) {
            if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
            currentEditOps.filter.sharpen = 0.0f;
        }
        binding.seekbarSharpen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float sharpen = progress / 100.0f; // Convert 0-100 to 0.0-1.0
                    Log.d("DocumentPageEdit", "Sharpen changed to: " + sharpen);
                    updateSharpen(sharpen);
                    binding.sharpenValue.setText(String.format("%.1f", sharpen));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Setup rotation button (icon only)
        binding.btnRotateRight.setOnClickListener(v -> rotateRight());

        // Setup crop button (icon only)
        binding.btnCrop.setOnClickListener(v -> showCropOptions());

        // Setup save and cancel buttons (icon only)
        binding.btnSave.setOnClickListener(v -> saveEdits());
        binding.btnCancel.setOnClickListener(v -> finish());
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

            // Set the current filter mode on the UI if it exists
            if (currentEditOps != null && currentEditOps.filter != null) {
                String mode = currentEditOps.filter.mode;
                updateFilterButtonSelection(mode);
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

    private void loadInitialImage() {
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            // Create a copy for the image view to prevent the original from being recycled by the library
            Bitmap previewBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            if (previewBitmap != null) {
                binding.imageViewPreview.setImage(ImageSource.bitmap(previewBitmap));
                Log.d("DocumentPageEdit", "Loaded initial image, applying filters");
                // Apply initial filters to show the current state
                applyFilters();
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
                            binding.imageViewPreview.setImage(ImageSource.bitmap(processedBitmap));
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