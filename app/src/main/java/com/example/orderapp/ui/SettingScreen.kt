package com.example.orderapp.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orderapp.AppTheme
import com.example.orderapp.model.ExportFormat
import com.example.orderapp.viewmodel.SettingViewModel
import com.example.orderapp.viewmodel.SettingViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = viewModel(factory = SettingViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentThemeState = viewModel.theme.collectAsState()
    println("SettingScreen recomposed. Current theme: ${currentThemeState.value}")

    var selectedExportFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var selectedImportFormat by remember { mutableStateOf(ExportFormat.JSON) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                scope.launch {
                    val dataString = viewModel.exportData(selectedExportFormat)
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(dataString.toByteArray())
                    }
                }
            }
        }
    }

// ...

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                scope.launch {
                    viewModel.importData(uri, selectedImportFormat)
                }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) { // スクロール可能にする
        Text("テーマ設定", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("※テーマの変更はアプリの再起動後に反映されます。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = when (currentThemeState.value) {
                    AppTheme.SYSTEM_DEFAULT -> "システムデフォルト"
                    AppTheme.LIGHT -> "ライトテーマ"
                    AppTheme.DARK -> "ダークテーマ"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("テーマ") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AppTheme.entries.forEach { themeOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (themeOption) {
                                    AppTheme.SYSTEM_DEFAULT -> "システムデフォルト"
                                    AppTheme.LIGHT -> "ライトテーマ"
                                    AppTheme.DARK -> "ダークテーマ"
                                }
                            )
                        },
                        onClick = {
                            viewModel.setTheme(themeOption)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("データ管理", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Export Format Selection
        Text("エクスポート形式", style = MaterialTheme.typography.titleMedium)
        Row {
            ExportFormat.entries.forEach { format ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (format == selectedExportFormat),
                        onClick = { selectedExportFormat = format },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(format.name)
                }
            }
        }
        Button(onClick = {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = if (selectedExportFormat == ExportFormat.JSON) "application/json" else "text/csv"
                putExtra(Intent.EXTRA_TITLE, if (selectedExportFormat == ExportFormat.JSON) "order_app_data.json" else "order_app_data.csv")
            }
            exportLauncher.launch(intent)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("データエクスポート")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Import Format Selection
        Text("インポート形式", style = MaterialTheme.typography.titleMedium)
        Row {
            ExportFormat.entries.forEach { format ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (format == selectedImportFormat),
                        onClick = { selectedImportFormat = format },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(format.name)
                }
            }
        }
        Button(onClick = {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // すべてのファイルタイプを選択可能にする
            }
            importLauncher.launch(intent)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("データインポート")
        }
        Spacer(modifier = Modifier.height(32.dp))
        var showChangelogDialog by remember { mutableStateOf(false) }

        Button(onClick = { showChangelogDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("更新履歴を表示")
        }
        Spacer(modifier = Modifier.height(32.dp))

        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                title = { Text("更新履歴") },
                text = {
                    Column {
                        ChangelogView()
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChangelogDialog = false }) {
                        Text("閉じる")
                    }
                }
            )
        }
        Text(text = "バージョン: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}", style = MaterialTheme.typography.bodySmall)
    }
}




