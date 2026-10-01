package jp.liftcho.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.liftcho.app.data.DaySummary
import jp.liftcho.app.data.Repo
import jp.liftcho.app.data.Seed
import kotlin.math.roundToInt

private val DAY_JP = mapOf(
    "mon" to "月", "tue" to "火", "wed" to "水",
    "thu" to "木", "fri" to "金", "sat" to "土", "sun" to "日"
)

@Composable
fun HistoryScreen(repo: Repo, modifier: Modifier = Modifier) {
    val history: List<DaySummary> by repo.history().collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        Column(modifier = modifier.fillMaxWidth().padding(24.dp)) {
            Text("まだ記録がありません", fontWeight = FontWeight.Medium)
            Text(
                "「記録」タブでセットを完了すると、ここに日ごとに並びます。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(history, key = { it.date + it.dayKey }) { day ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        day.date.substring(5).replace("-", "/"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text(
                        "${DAY_JP[day.dayKey].orEmpty()} ${Seed.dayLabels[day.dayKey].orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${day.doneSets} セット · ${day.volume.roundToInt()} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
