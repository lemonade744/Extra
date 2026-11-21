package com.lemonade.extra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemonade.extra.ui.MainViewModel

class MainActivity : ComponentActivity() {
    // Подключаем ViewModel к Activity
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Твоя стандартная тема (если она есть в проекте, оберни в неё)
            // Surface - это просто фон приложения
            Surface(modifier = Modifier.fillMaxSize()) {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val exercises by viewModel.exercises.collectAsState()
    val activeWorkoutId by viewModel.activeWorkoutId.collectAsState()

    // Если тренировка активна, показываем экран тренировки
    if (activeWorkoutId != null) {
        ActiveWorkoutScreen(workoutId = activeWorkoutId!!, onFinish = { viewModel.finishWorkout() })
    } else {
        // Иначе показываем главный экран
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Справочник упражнений",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(exercises) { exercise ->
                    ExerciseCard(exercise.name, exercise.type)
                }
            }
            Button(
                onClick = { viewModel.startWorkout() }, // Запуск
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Начать тренировку", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
@Composable
fun ExerciseCard(name: String, type: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Тип: $type", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ActiveWorkoutScreen(workoutId: Long, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Тренировка началась!", style = MaterialTheme.typography.headlineMedium)
        Text("ID тренировки: $workoutId", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onFinish) {
            Text("Завершить тренировку")
        }
    }
}