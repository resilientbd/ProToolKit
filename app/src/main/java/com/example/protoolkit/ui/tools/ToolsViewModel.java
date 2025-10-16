package com.example.protoolkit.ui.tools;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.R;
import com.example.protoolkit.domain.model.ToolItem;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.AppConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Presents the complete tool catalog on the Tools screen.
 */
public class ToolsViewModel extends BaseViewModel {

    private final MutableLiveData<List<ToolItem>> tools = new MutableLiveData<>();

    public ToolsViewModel(@NonNull Application application) {
        List<ToolItem> items = new ArrayList<>();
        items.add(new ToolItem(AppConstants.TOOL_ID_UNIT_CONVERTER, R.string.label_unit_converter,
                R.drawable.ic_tool_unit, R.id.unitConverterFragment, false));
        items.add(new ToolItem(AppConstants.TOOL_ID_TEXT_TOOLS, R.string.label_text_tools,
                R.drawable.ic_tool_text, R.id.textToolsFragment, false));
        items.add(new ToolItem(AppConstants.TOOL_ID_DEVICE_INFO, R.string.label_device_info,
                R.drawable.ic_tool_device, R.id.deviceInfoFragment, false));
        items.add(new ToolItem(AppConstants.TOOL_ID_QR_SCANNER, R.string.label_qr_scanner,
                R.drawable.ic_tool_qr, R.id.qrScannerFragment, true));
        items.add(new ToolItem(AppConstants.TOOL_ID_FILE_TOOLS, R.string.label_file_tools,
                R.drawable.ic_tool_file, R.id.fileToolsFragment, false));
        items.add(new ToolItem(AppConstants.TOOL_ID_NETWORK_TOOLS, R.string.label_network_tools,
                R.drawable.ic_tool_network, R.id.networkToolsFragment, false));
        tools.setValue(Collections.unmodifiableList(items));
    }

    public LiveData<List<ToolItem>> getTools() {
        return tools;
    }
}
