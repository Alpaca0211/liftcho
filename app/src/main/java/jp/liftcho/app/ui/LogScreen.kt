package jp.liftcho.app.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.liftcho.app.data.Exercise
import jp.liftcho.app.data.Repo
import jp.liftcho.app.data.Seed
import jp.liftcho.app.data.SetLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

private val DAY_JP = mapOf(
    "mon" to "月", "tue" to "火", "wed" to "水",
    "thu" to "木", "fri" to "金", "sat" to "土", "sun" to "日"
)

private fun dayKeyOf(date: LocalDate): String = when (date.dayOfWeek.value) {
    1 -> "mon"; 2 -> "tue"; 3 -> "wed"; 4 -> "thu"; 5 -> "fri"; 6 -> "sat"; else -> "sun"
}

private fun fmtWeight(w: Double?): String =
    if (w == null) "自重" else if (w % 1.0 == 0.0) w.toInt().toString() else ((w * 100).roundToInt() / 100.0).toString()

@Composable
fun LogScreen(repo: Repo, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var date by remember { mutableStateOf(LocalDate.now()) }
    var dayKey by remember {
        mutableStateOf(
            dayKeyOf(LocalDate.now()).takeIf { Seed.dayLabels.containsKey(it) } ?: "mon"
        )
    }
    val dateText = date.format(DateTimeFormatter.ofPattern("M/d"))

    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var logs by remember { mutableStateOf<List<SetLog>>(emptyList()) }
    var previous by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(dayKey) {
        repo.exercises(dayKey).collectLatest { exercises = it }
    }
    LaunchedEffect(date, dayKey, exercises.size) {
        if (exercises.isNotEmpty()) repo.ensureSession(date.toString(), dayKey)
    }
    LaunchedEffect(date, dayKey) {
        repo.logs(date.toString(), dayKey).collectLatest { logs = it }
    }
    LaunchedEffect(exercises, date) {
        previous = exercises.associate { e ->
            val sets = repo.previousSession(e.id, date.toString())
            e.id to if (sets.isEmpty()) "" else {
                val d = sets.first().date.substring(5).replace("-", "/")
                "前回 $d ${fmtWeight(sets.first().weight)}kg × " + sets.joinToString(", ") { it.reps.toString() }
            }
        }
    }

    // 休憩タイマー
    var restEndAt by remember { mutableLongStateOf(0L) }
    var restTotal by remember { mutableLongStateOf(0L) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(restEndAt) {
        if (restEndAt == 0L) return@LaunchedEffect
        while (System.currentTimeMillis() < restEndAt) {
            nowMs = System.currentTimeMillis()
            delay(250)
        }
        nowMs = System.currentTimeMillis()
        vibrate(context)
        delay(8000)
        if (System.currentTimeMillis() >= restEndAt) restEndAt = 0L
    }
    val remainMs = max(0L, restEndAt - nowMs)

    val doneCount = logs.count { it.done }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { date = date.minusDays(1) }) { Text("前日") }
            Text(
                "$dateText（${DAY_JP[dayKeyOf(date)]}）",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TextButton(onClick = { date = date.plusDays(1) }) { Text("翌日") }
            Spacer(Modifier.weight(1f))
            Text(
                "$doneCount / ${logs.size} セット",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Seed.dayLabels.forEach { (key, label) ->
                val selected = key == dayKey
                if (selected) {
                    Button(onClick = { }, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                        Text("${DAY_JP[key]} $label", fontSize = 13.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { dayKey = key },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("${DAY_JP[key]} $label", fontSize = 13.sp)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(exercises, key = { it.id }) { ex ->
                val rows = logs.filter { it.exerciseId == ex.id }.sortedBy { it.setNo }
                ExerciseCard(
                    ex = ex,
                    rows = rows,
                    previousText = previous[ex.id].orEmpty(),
                    onWeight = { row, delta ->
                        val base = row.weight ?: 0.0
                        val next = max(0.0, base + delta)
                        scope.launch { repo.spreadWeight(logs, row, next) }
                    },
                    onReps = { row, delta ->
                        scope.launch { repo.setReps(row, max(0, row.reps + delta)) }
                    },
                    onToggle = { row ->
                        scope.launch { repo.toggleDone(row) }
                        if (!row.done) {
                            restTotal = ex.restSec * 1000L
                            restEndAt = System.currentTimeMillis() + restTotal
                        }
                    },
                    onAddSet = { scope.launch { repo.addSet(ex, logs) } }
                )
            }
        }
    }

    if (restEndAt > 0L) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { if (restTotal == 0L) 0f else 1f - remainMs.toFloat() / restTotal },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sec = (remainMs / 1000).toInt()
                        Text(
                            "%d:%02d".format(sec / 60, sec % 60),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(if (sec > 0) "休憩中" else "休憩終了", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { restEndAt += 30_000; restTotal += 30_000 }) { Text("+30秒") }
                        TextButton(onClick = { restEndAt = 0L }) { Text("閉じる") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    ex: Exercise,
    rows: List<SetLog>,
    previousText: String,
    onWeight: (SetLog, Double) -> Unit,
    onReps: (SetLog, Int) -> Unit,
    onToggle: (SetLog) -> Unit,
    onAddSet: () -> Unit
) {
    val complete = rows.isNotEmpty() && rows.all { it.done }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (complete) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(ex.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(
                "目標 ${fmtWeight(ex.targetWeight)}${if (ex.targetWeight == null) "" else "kg"} × " +
                    "${ex.repMin}–${ex.repMax}回 × ${ex.sets}セット",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (previousText.isNotEmpty()) {
                Text(
                    previousText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${row.setNo}",
                        modifier = Modifier.width(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    if (row.weight == null) {
                        Text("自重", modifier = Modifier.width(112.dp), fontSize = 15.sp)
                    } else {
                        Stepper(
                            value = "${fmtWeight(row.weight)}kg",
                            onMinus = { onWeight(row, -ex.step) },
                            onPlus = { onWeight(row, ex.step) }
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Stepper(
                        value = "${row.reps}回",
                        onMinus = { onReps(row, -1) },
                        onPlus = { onReps(row, 1) }
                    )
                    Spacer(Modifier.weight(1f))
                    if (row.done) {
                        Button(
                            onClick = { onToggle(row) },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) { Text("✓") }
                    } else {
                        OutlinedButton(
                            onClick = { onToggle(row) },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) { Text("✓") }
                    }
                }
            }
            TextButton(onClick = onAddSet, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                Text("＋ セット", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = onMinus,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(width = 36.dp, height = 36.dp),
            colors = ButtonDefaults.outlinedButtonColors()
        ) { Text("−", fontSize = 16.sp) }
        Text(
            value,
            modifier = Modifier.width(62.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        OutlinedButton(
            onClick = onPlus,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(width = 36.dp, height = 36.dp)
        ) { Text("+", fontSize = 16.sp) }
    }
}

private fun vibrate(context: android.content.Context) {
    runCatching {
        val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(effect)
        }
    }
}
