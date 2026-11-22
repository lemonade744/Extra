package com.lemonade.extra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemonade.extra.data.entity.*
import com.lemonade.extra.ui.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val exercises by viewModel.exercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()
    val workoutState by viewModel.workoutState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    val user by viewModel.user.collectAsState()
    val selectedExercises by viewModel.selectedExercises.collectAsState()
    val workoutHistory by viewModel.workoutHistory.collectAsState()

    val activeWorkoutExercises by viewModel.activeWorkoutExercises.collectAsState()
    val currentIndex by viewModel.currentExerciseIndex.collectAsState()
    val workoutTimerSec by viewModel.workoutTimerSec.collectAsState()
    val restTimerSec by viewModel.restTimerSec.collectAsState()
    val isResting by viewModel.isResting.collectAsState()
    val isSetRunning by viewModel.isSetRunning.collectAsState()
    val setTimerSec by viewModel.setTimerSec.collectAsState()

    val analyticsPeriod by viewModel.analyticsPeriod.collectAsState()
    val timeChartData by viewModel.timeChartData.collectAsState()
    val intensityChartData by viewModel.intensityChartData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (workoutState) {
                        WorkoutState.SETUP -> Text("Настройка тренировки")
                        WorkoutState.ACTIVE -> Text("Тренировка")
                        WorkoutState.IDLE -> when (currentTab) {
                            AppTab.HOME -> Text("Справочник")
                            AppTab.LK -> Text("Личный кабинет")
                            AppTab.ANALYTICS -> Text("Аналитика")
                            AppTab.SETTINGS -> Text("Настройки")
                        }
                    }
                },
                navigationIcon = {
                    if (workoutState == WorkoutState.SETUP) {
                        IconButton(onClick = { viewModel.cancelWorkoutSetup() }) {
                            Icon(Icons.Default.Close, contentDescription = "Отмена")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (workoutState == WorkoutState.IDLE) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { viewModel.selectTab(AppTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
                        label = { Text("Главная") }
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.LK,
                        onClick = { viewModel.selectTab(AppTab.LK) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "ЛК") },
                        label = { Text("ЛК") }
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.ANALYTICS,
                        onClick = { viewModel.selectTab(AppTab.ANALYTICS) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Аналитика") },
                        label = { Text("Аналитика") }
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                        label = { Text("Настройки") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (workoutState) {
                WorkoutState.SETUP -> WorkoutSetupScreen(
                    muscleGroups = muscleGroups,
                    exercises = exercises,
                    selectedExercises = selectedExercises,
                    onToggleExercise = { viewModel.toggleExerciseForSetup(it) },
                    onUpdateSetup = { id, r, d, dist, w -> viewModel.updateExerciseSetup(id, r, d, dist, w) },
                    onStart = { viewModel.startWorkout() }
                )
                WorkoutState.ACTIVE -> ActiveWorkoutScreen(
                    exercises = activeWorkoutExercises,
                    setupInfo = selectedExercises,
                    currentIndex = currentIndex,
                    workoutTimer = workoutTimerSec,
                    restTimer = restTimerSec,
                    isResting = isResting,
                    isSetRunning = isSetRunning,
                    setTimer = setTimerSec,
                    onSelectExercise = { viewModel.setCurrentExercise(it) },
                    onStartSet = { viewModel.startSet() },
                    onCompleteSet = { r, d, dist -> viewModel.completeSet(r, d, dist) },
                    onSkipRest = { viewModel.skipRest() },
                    onAdjustRest = { delta -> viewModel.adjustRestDuration(delta) },
                    onUpdateSetup = { id, r, d, dist, w -> viewModel.updateExerciseSetup(id, r, d, dist, w) },
                    onFinish = { viewModel.finishWorkout(8) }
                )
                WorkoutState.IDLE -> {
                    when (currentTab) {
                        AppTab.HOME -> HomeScreen(exercises, onStartWorkout = { viewModel.openWorkoutSetup() })
                        AppTab.LK -> LkScreen(user, workoutHistory, onUpdateProfile = { w, h -> viewModel.updateProfile(w, h) })
                        AppTab.ANALYTICS -> AnalyticsScreen(
                            period = analyticsPeriod,
                            timeData = timeChartData,
                            intensityData = intensityChartData,
                            workoutHistory = workoutHistory,
                            onPeriodChange = { viewModel.setAnalyticsPeriod(it) }
                        )
                        AppTab.SETTINGS -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutSetupScreen(
    muscleGroups: List<MuscleGroupEntity>,
    exercises: List<ExerciseEntity>,
    selectedExercises: List<ExerciseSetup>,
    onToggleExercise: (ExerciseEntity) -> Unit,
    onUpdateSetup: (Long, Int, Int, Double, Double) -> Unit,
    onStart: () -> Unit
) {
    val expandedGroups = remember { mutableStateMapOf<Long, Boolean>() }
    val uncategorizedExpanded = remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Выберите упражнения", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(muscleGroups) { group ->
                val groupExercises = exercises.filter { it.primaryMuscleGroupId == group.id }
                val isExpanded = expandedGroups[group.id] ?: false

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { expandedGroups[group.id] = !isExpanded }.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        if (isExpanded) {
                            groupExercises.forEach { exercise ->
                                val setup = selectedExercises.find { it.exercise.id == exercise.id }
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onToggleExercise(exercise) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = setup != null, onCheckedChange = { onToggleExercise(exercise) })
                                        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                                    }

                                    if (setup != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            when (exercise.type) {
                                                "STRENGTH", "BODYWEIGHT" -> {
                                                    OutlinedTextField(
                                                        value = setup.targetReps.toString(),
                                                        onValueChange = { onUpdateSetup(exercise.id, it.toIntOrNull() ?: 0, setup.targetDurationSec, setup.targetDistanceM, setup.additionalWeight) },
                                                        label = { Text("Повторы") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = setup.additionalWeight.toString(),
                                                        onValueChange = { onUpdateSetup(exercise.id, setup.targetReps, setup.targetDurationSec, setup.targetDistanceM, it.toDoubleOrNull() ?: 0.0) },
                                                        label = { Text("Доп. вес") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                "TIMED" -> {
                                                    OutlinedTextField(
                                                        value = setup.targetDurationSec.toString(),
                                                        onValueChange = { onUpdateSetup(exercise.id, setup.targetReps, it.toIntOrNull() ?: 0, setup.targetDistanceM, setup.additionalWeight) },
                                                        label = { Text("Секунды") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = setup.additionalWeight.toString(),
                                                        onValueChange = { onUpdateSetup(exercise.id, setup.targetReps, setup.targetDurationSec, setup.targetDistanceM, it.toDoubleOrNull() ?: 0.0) },
                                                        label = { Text("Доп. вес") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                "RUNNING" -> {
                                                    OutlinedTextField(
                                                        value = setup.targetDistanceM.toString(),
                                                        onValueChange = { onUpdateSetup(exercise.id, setup.targetReps, setup.targetDurationSec, it.toDoubleOrNull() ?: 0.0, setup.additionalWeight) },
                                                        label = { Text("Дистанция (м)") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Divider(modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }

            val uncategorized = exercises.filter { it.primaryMuscleGroupId == null }
            if (uncategorized.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { uncategorizedExpanded.value = !uncategorizedExpanded.value }.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Без группы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (uncategorizedExpanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }

                            if (uncategorizedExpanded.value) {
                                uncategorized.forEach { exercise ->
                                    val setup = selectedExercises.find { it.exercise.id == exercise.id }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onToggleExercise(exercise) }.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = setup != null, onCheckedChange = { onToggleExercise(exercise) })
                                        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Начать тренировку")
        }
    }
}

@Composable
fun ActiveWorkoutScreen(
    exercises: List<WorkoutExerciseEntity>,
    setupInfo: List<ExerciseSetup>,
    currentIndex: Int,
    workoutTimer: Int,
    restTimer: Int,
    isResting: Boolean,
    isSetRunning: Boolean,
    setTimer: Int,
    onSelectExercise: (Int) -> Unit,
    onStartSet: () -> Unit,
    onCompleteSet: (Int?, Int?, Double?) -> Unit,
    onSkipRest: () -> Unit,
    onAdjustRest: (Int) -> Unit,
    onUpdateSetup: (Long, Int, Int, Double, Double) -> Unit,
    onFinish: () -> Unit
) {
    val currentWe = exercises.getOrNull(currentIndex)
    val currentSetup = setupInfo.getOrNull(currentIndex)

    var inputValue by remember(currentIndex) {
        mutableStateOf(
            when (currentSetup?.exercise?.type) {
                "TIMED" -> currentSetup.targetDurationSec.toString()
                "RUNNING" -> currentSetup.targetDistanceM.toString()
                else -> currentSetup?.targetReps?.toString() ?: "10"
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", workoutTimer / 60, workoutTimer % 60),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (currentWe != null && currentSetup != null) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Упражнение ${currentIndex + 1} из ${exercises.size}", style = MaterialTheme.typography.titleMedium)
                Text(currentSetup.exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(32.dp))

                if (isSetRunning) {
                    Text("Выполняется подход", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                    if (currentSetup.exercise.type == "TIMED") {
                        Text("$setTimer сек", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(String.format(Locale.getDefault(), "%02d:%02d", setTimer / 60, setTimer % 60), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (currentSetup.exercise.type == "STRENGTH" || currentSetup.exercise.type == "BODYWEIGHT") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputValue,
                                onValueChange = { inputValue = it },
                                label = { Text("Повторы") },
                                modifier = Modifier.width(120.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (currentSetup.exercise.type == "RUNNING") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputValue,
                                onValueChange = { inputValue = it },
                                label = { Text("Дистанция (м)") },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    IconButton(
                        onClick = {
                            when (currentSetup.exercise.type) {
                                "TIMED" -> onCompleteSet(null, setTimer, null)
                                "RUNNING" -> onCompleteSet(null, setTimer, inputValue.toDoubleOrNull() ?: 0.0)
                                else -> onCompleteSet(inputValue.toIntOrNull() ?: 0, null, null)
                            }
                        },
                        modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Завершить подход", tint = Color.White, modifier = Modifier.size(40.dp))
                    }

                } else if (isResting) {
                    Text("Отдых", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("$restTimer сек", fontSize = 36.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OutlinedButton(onClick = { onAdjustRest(-30) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("-30") }
                        OutlinedButton(onClick = { onAdjustRest(-15) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("-15") }
                        OutlinedButton(onClick = { onAdjustRest(15) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("+15") }
                        OutlinedButton(onClick = { onAdjustRest(30) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("+30") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onSkipRest) { Text("Пропустить отдых и начать подход") }

                    if (exercises.size > 1) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Следующее упражнение:", style = MaterialTheme.typography.titleMedium)
                        LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(exercises) { index, _ ->
                                val setup = setupInfo.getOrNull(index)
                                FilterChip(
                                    selected = index == currentIndex,
                                    onClick = { onSelectExercise(index) },
                                    label = { Text(setup?.exercise?.name ?: "Упр. ${index+1}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Настройки для этого упражнения:", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            when (currentSetup.exercise.type) {
                                "STRENGTH", "BODYWEIGHT" -> {
                                    OutlinedTextField(
                                        value = currentSetup.targetReps.toString(),
                                        onValueChange = { onUpdateSetup(currentSetup.exercise.id, it.toIntOrNull() ?: 0, currentSetup.targetDurationSec, currentSetup.targetDistanceM, currentSetup.additionalWeight) },
                                        label = { Text("Повторы") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = currentSetup.additionalWeight.toString(),
                                        onValueChange = { onUpdateSetup(currentSetup.exercise.id, currentSetup.targetReps, currentSetup.targetDurationSec, currentSetup.targetDistanceM, it.toDoubleOrNull() ?: 0.0) },
                                        label = { Text("Доп. вес") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                "TIMED" -> {
                                    OutlinedTextField(
                                        value = currentSetup.targetDurationSec.toString(),
                                        onValueChange = { onUpdateSetup(currentSetup.exercise.id, currentSetup.targetReps, it.toIntOrNull() ?: 0, currentSetup.targetDistanceM, currentSetup.additionalWeight) },
                                        label = { Text("Секунды") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                "RUNNING" -> {
                                    OutlinedTextField(
                                        value = currentSetup.targetDistanceM.toString(),
                                        onValueChange = { onUpdateSetup(currentSetup.exercise.id, currentSetup.targetReps, currentSetup.targetDurationSec, it.toDoubleOrNull() ?: 0.0, currentSetup.additionalWeight) },
                                        label = { Text("Дистанция (м)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                } else {
                    Text("Готовы к подходу", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    IconButton(
                        onClick = onStartSet,
                        modifier = Modifier.size(96.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Начать подход", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Завершить тренировку")
        }
    }
}

@Composable
fun AnalyticsScreen(
    period: AnalyticsPeriod,
    timeData: List<ChartPoint>,
    intensityData: List<ChartPoint>,
    workoutHistory: List<WorkoutEntity>,
    onPeriodChange: (AnalyticsPeriod) -> Unit
) {
    val totalWorkouts = workoutHistory.size
    val totalTimeSec = workoutHistory.sumOf { it.totalDurationSec ?: 0 }
    val totalVolume = workoutHistory.sumOf { it.totalVolume ?: 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnalyticsPeriod.values().forEach { p ->
                FilterChip(
                    selected = period == p,
                    onClick = { onPeriodChange(p) },
                    label = {
                        Text(when(p) {
                            AnalyticsPeriod.WEEK -> "Неделя"
                            AnalyticsPeriod.MONTH -> "Месяц"
                            AnalyticsPeriod.YEAR -> "Год"
                            AnalyticsPeriod.ALL -> "Все"
                        })
                    }
                )
            }
        }

        Text("Общая статистика", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Тренировок", "$totalWorkouts", Modifier.weight(1f))
            StatCard("Время (мин)", "${totalTimeSec / 60}", Modifier.weight(1f))
            StatCard("Нагрузка (кг)", String.format(Locale.getDefault(), "%.0f", totalVolume), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Время тренировок (мин)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        BarChart(data = timeData, showStatic = false)

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Интенсивность (кг/мин)", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Динам.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Стат.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BarChart(data = intensityData, showStatic = true)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BarChart(data: List<ChartPoint>, showStatic: Boolean) {
    if (data.isEmpty()) {
        Text("Нет данных для отображения", color = Color.Gray)
        return
    }

    // Независимые максимумы для правильного масштабирования
    val maxDyn = data.maxOfOrNull { it.dynamicValue }?.coerceAtLeast(1f) ?: 1f
    val maxStat = if (showStatic) data.maxOfOrNull { it.staticValue }?.coerceAtLeast(1f) ?: 1f else 1f

    var selectedPointInfo by remember { mutableStateOf<String?>(null) }

    LazyRow(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        items(data) { point ->
            val hasStatic = showStatic && point.staticValue > 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Столбик динамики
                    val dynHeight = (point.dynamicValue / maxDyn * 120).dp.coerceAtLeast(4.dp)
                    Box(
                        modifier = Modifier
                            .width(if (hasStatic) 16.dp else 32.dp)
                            .height(dynHeight)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .clickable {
                                selectedPointInfo = "Дата: ${point.label}\nДинамика: ${String.format(Locale.getDefault(), "%.1f", point.dynamicValue)}"
                            }
                    )

                    // Столбик статики (если есть)
                    if (hasStatic) {
                        val statHeight = (point.staticValue / maxStat * 120).dp.coerceAtLeast(4.dp)
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(statHeight)
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .clickable {
                                    selectedPointInfo = "Дата: ${point.label}\nСтатика: ${String.format(Locale.getDefault(), "%.1f", point.staticValue)}"
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }

    // Всплывающее окно с деталями
    selectedPointInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { selectedPointInfo = null },
            title = { Text("Детали нагрузки") },
            text = { Text(info) },
            confirmButton = {
                TextButton(onClick = { selectedPointInfo = null }) {
                    Text("ОК")
                }
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun HomeScreen(exercises: List<ExerciseEntity>, onStartWorkout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(exercises) { exercise ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                        Text("Тип: ${exercise.type}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Button(onClick = onStartWorkout, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Начать тренировку")
        }
    }
}

@Composable
fun LkScreen(user: UserEntity?, workoutHistory: List<WorkoutEntity>, onUpdateProfile: (Double, Double) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Профиль", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Вес: ${user?.weightKg ?: "--"} кг")
                Text("Рост: ${user?.heightCm ?: "--"} см")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showEditDialog = true }) { Text("Изменить данные") }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Активность (последние 28 дней)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Heatmap(workoutHistory)
    }
    if (showEditDialog && user != null) {
        EditProfileDialog(user, onDismiss = { showEditDialog = false }, onSave = { w, h -> onUpdateProfile(w, h); showEditDialog = false })
    }
}

@Composable
fun Heatmap(workouts: List<WorkoutEntity>) {
    val workoutDates = workouts.map { it.date }.toSet()
    val calendar = Calendar.getInstance()
    val days = mutableListOf<String>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    for (i in 0 until 28) { days.add(sdf.format(calendar.time)); calendar.add(Calendar.DAY_OF_YEAR, -1) }
    days.reverse()
    Column {
        for (week in 0 until 4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (day in 0 until 7) {
                    val date = days[week * 7 + day]
                    val isActive = workoutDates.contains(date)
                    Box(modifier = Modifier.size(36.dp).background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun EditProfileDialog(user: UserEntity, onDismiss: () -> Unit, onSave: (Double, Double) -> Unit) {
    var weight by remember { mutableStateOf(user.weightKg.toString()) }
    var height by remember { mutableStateOf(user.heightCm.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Данные тела") },
        text = {
            Column {
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Вес (кг)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Рост (см)") })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(weight.toDoubleOrNull() ?: user.weightKg, height.toDoubleOrNull() ?: user.heightCm) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Настройки профиля", style = MaterialTheme.typography.headlineSmall)
    }
}