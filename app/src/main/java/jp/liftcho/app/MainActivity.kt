package jp.liftcho.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import jp.liftcho.app.data.AppDb
import jp.liftcho.app.data.Repo
import jp.liftcho.app.ui.HistoryScreen
import jp.liftcho.app.ui.LiftchoTheme
import jp.liftcho.app.ui.LogScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = Repo(AppDb.get(this).dao())
        setContent {
            LiftchoTheme {
                AppRoot(repo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(repo: Repo) {
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { repo.seedIfEmpty() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("リフト帳") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Text("記") },
                    label = { Text("記録") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Text("履") },
                    label = { Text("履歴") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            when (tab) {
                0 -> LogScreen(repo, Modifier.weight(1f))
                else -> HistoryScreen(repo, Modifier.weight(1f))
            }
        }
    }
}
