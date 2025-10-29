package com.faisal.protoolkit.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.data.settings.SettingsRepository;
import com.faisal.protoolkit.domain.model.ToolItem;
import com.faisal.protoolkit.ui.base.BaseViewModel;
import com.faisal.protoolkit.util.AppConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Supplies HomeFragment with tool data and search filtering.
 */
public class HomeViewModel extends BaseViewModel {

    private final Application application;
    private final SettingsRepository settingsRepository;
    private final List<ToolItem> allTools = new ArrayList<>();
    private final MutableLiveData<List<ToolItem>> visibleTools = new MutableLiveData<>();
    private final MutableLiveData<Boolean> adsVisible = new MutableLiveData<>(true);

    public HomeViewModel(@NonNull Application application, @NonNull SettingsRepository settingsRepository) {
        this.application = application;
        this.settingsRepository = settingsRepository;
        buildTools();
        filter("");
        adsVisible.setValue(settingsRepository.shouldShowAds());
    }

    private void buildTools() {
        allTools.clear();
        allTools.add(new ToolItem(AppConstants.TOOL_ID_DOC_SCANNER, R.string.label_doc_scanner,
                R.drawable.ic_tool_file, R.id.documentScannerFragment, true));
        allTools.add(new ToolItem(AppConstants.TOOL_ID_NETWORK_TOOLS, R.string.label_network_tools,
                R.drawable.ic_tool_network, R.id.networkToolsFragment, false));

        allTools.add(new ToolItem(AppConstants.TOOL_ID_DEVICE_INFO, R.string.label_device_info,
                R.drawable.ic_tool_device, R.id.deviceInfoFragment, false));

        allTools.add(new ToolItem(AppConstants.TOOL_ID_QR_SCANNER, R.string.label_qr_scanner,
                R.drawable.ic_tool_qr, R.id.qrScannerFragment, true));
        allTools.add(new ToolItem(AppConstants.TOOL_ID_FILE_TOOLS, R.string.label_file_tools,
                R.drawable.ic_tool_file, R.id.fileToolsFragment, false));
        allTools.add(new ToolItem(AppConstants.TOOL_ID_TEXT_TOOLS, R.string.label_text_tools,
                R.drawable.ic_tool_text, R.id.textToolsFragment, false));
        allTools.add(new ToolItem(AppConstants.TOOL_ID_UNIT_CONVERTER, R.string.label_unit_converter,
                R.drawable.ic_tool_unit, R.id.unitConverterFragment, false));





    }

    public LiveData<List<ToolItem>> getVisibleTools() {
        return visibleTools;
    }

    public LiveData<Boolean> areAdsVisible() {
        return adsVisible;
    }

    public void filter(@NonNull String query) {
        if (query.trim().isEmpty()) {
            visibleTools.setValue(Collections.unmodifiableList(new ArrayList<>(allTools)));
            return;
        }
        String lowerQuery = query.toLowerCase(Locale.getDefault());
        List<ToolItem> filtered = new ArrayList<>();
        for (ToolItem item : allTools) {
            String label = application.getString(item.getTitleRes()).toLowerCase(Locale.getDefault());
            if (label.contains(lowerQuery) || item.getId().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                filtered.add(item);
            }
        }
        visibleTools.setValue(Collections.unmodifiableList(filtered));
    }

    public void refreshAdsState() {
        adsVisible.setValue(settingsRepository.shouldShowAds());
    }
}
