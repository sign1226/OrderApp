package com.example.orderapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.orderapp.model.getChangelog

@Composable
fun ChangelogView() {
    val changelogs = getChangelog()
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        changelogs.forEach { changelog ->
            Text(
                text = "バージョン: ${changelog.version}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                changelog.changes.forEach { change ->
                    Text(text = "・$change", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
