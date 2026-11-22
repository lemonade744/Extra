package com.lemonade.extra.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lemonade.extra.data.database.AppDatabase
import com.lemonade.extra.data.dao.SetWithType
import com.lemonade.extra.data.entity.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab { HOME, LK, ANALYTICS, SETTINGS }
enum class WorkoutState { IDLE, SETUP, ACTIVE }
enum class AnalyticsPeriod { WEEK, MONTH, YEAR, ALL }

data class ExerciseSetup(
    val exercise: ExerciseEntity,
    val targetReps: Int = 10,
    val targetDurationSec: Int = 60,
    val targetDistanceM: Double = 1000.0,
    val additionalWeight: Double = 0.0
)

data class ChartPoint(
    val label: String,
    val dynamicValue: Float = 0f,
    val staticValue: Float = 0f
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _exercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    val exercises: StateFlow<List<ExerciseEntity>> = _exercises

    private val _muscleGroups = MutableStateFlow<List<MuscleGroupEntity>>(emptyList())
    val muscleGroups: StateFlow<List<MuscleGroupEntity>> = _muscleGroups

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState: StateFlow<WorkoutState> = _workoutState

    private val _selectedExercises = MutableStateFlow<List<ExerciseSetup>>(emptyList())
    val selectedExercises: StateFlow<List<ExerciseSetup>> = _selectedExercises

    private val _activeWorkoutId = MutableStateFlow<Long?>(null)
    val activeWorkoutId: StateFlow<Long?> = _activeWorkoutId

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex

    private val _activeWorkoutExercises = MutableStateFlow<List<WorkoutExerciseEntity>>(emptyList())
    val activeWorkoutExercises: StateFlow<List<WorkoutExerciseEntity>> = _activeWorkoutExercises

    private val _workoutTimerSec = MutableStateFlow(0)
    val workoutTimerSec: StateFlow<Int> = _workoutTimerSec

    private val _restTimerSec = MutableStateFlow(0)
    val restTimerSec: StateFlow<Int> = _restTimerSec

    private val _restDurationSec = MutableStateFlow(90)
    val restDurationSec: StateFlow<Int> = _restDurationSec

    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting

    private val _isSetRunning = MutableStateFlow(false)
    val isSetRunning: StateFlow<Boolean> = _isSetRunning

    private val _setTimerSec = MutableStateFlow(0)
    val setTimerSec: StateFlow<Int> = _setTimerSec

    private val _workoutHistory = MutableStateFlow<List<WorkoutEntity>>(emptyList())
    val workoutHistory: StateFlow<List<WorkoutEntity>> = _workoutHistory

    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val analyticsPeriod: StateFlow<AnalyticsPeriod> = _analyticsPeriod

    private val _timeChartData = MutableStateFlow<List<ChartPoint>>(emptyList())
    val timeChartData: StateFlow<List<ChartPoint>> = _timeChartData

    private val _intensityChartData = MutableStateFlow<List<ChartPoint>>(emptyList())
    val intensityChartData: StateFlow<List<ChartPoint>> = _intensityChartData

    private var timerJob: Job? = null
    private var restJob: Job? = null
    private var setJob: Job? = null

    init {
        loadInitialData()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
        updateAnalyticsData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _exercises.value = db.exerciseDao().getAllExercises()
            _muscleGroups.value = db.muscleGroupDao().getAllGroupsList()
            _user.value = db.userDao().getUser()
            _workoutHistory.value = db.workoutDao().getFinishedWorkouts()
            updateAnalyticsData()
        }
    }

    private fun updateAnalyticsData() {
        val history = _workoutHistory.value
        val period = _analyticsPeriod.value

        val now = Calendar.getInstance()
        val filteredHistory = history.filter { workout ->
            val parts = workout.date.split("-")
            if (parts.size != 3) return@filter false
            val workoutDate = Calendar.getInstance().apply {
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }

            when (period) {
                AnalyticsPeriod.WEEK -> {
                    val diffInMillis = now.timeInMillis - workoutDate.timeInMillis
                    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                    diffInDays in 0..7
                }
                AnalyticsPeriod.MONTH -> {
                    workoutDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            workoutDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                }
                AnalyticsPeriod.YEAR -> {
                    workoutDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
                AnalyticsPeriod.ALL -> true
            }
        }

        val groupedTime = mutableMapOf<String, Float>()
        val groupedDynIntensity = mutableMapOf<String, Float>()
        val groupedStatIntensity = mutableMapOf<String, Float>()

        val sdfMonth = SimpleDateFormat("MMM", Locale("ru"))
        val sdfDay = SimpleDateFormat("dd.MM", Locale.getDefault())
        val sdfMonthYear = SimpleDateFormat("MM.yy", Locale.getDefault())

        viewModelScope.launch {
            val allSets = db.workoutDao().getAllSetsWithType()

            filteredHistory.forEach { workout ->
                val parts = workout.date.split("-")
                if (parts.size != 3) return@forEach
                val dateObj = Calendar.getInstance().apply {
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }

                val timeMin = (workout.totalDurationSec ?: 0) / 60f

                val label = when (period) {
                    AnalyticsPeriod.WEEK -> sdfDay.format(dateObj.time)
                    AnalyticsPeriod.MONTH -> {
                        val weekOfMonth = (dateObj.get(Calendar.DAY_OF_MONTH) - 1) / 7 + 1
                        "Нед $weekOfMonth"
                    }
                    AnalyticsPeriod.YEAR -> sdfMonth.format(dateObj.time)
                    AnalyticsPeriod.ALL -> sdfMonthYear.format(dateObj.time)
                }

                val workoutSets = allSets.filter { it.workoutId == workout.id }

                var dynVolume = 0f
                var statVolume = 0f

                workoutSets.forEach { set ->
                    val weight = set.weightKg?.toFloat() ?: 0f
                    if (set.type == "TIMED") {
                        statVolume += (set.durationSec ?: 0) * weight
                    } else {
                        dynVolume += (set.reps ?: 0) * weight
                    }
                }

                val dynInt = if (timeMin > 0) dynVolume / timeMin else 0f
                val statInt = if (timeMin > 0) statVolume / timeMin else 0f

                groupedTime[label] = (groupedTime[label] ?: 0f) + timeMin
                groupedDynIntensity[label] = (groupedDynIntensity[label] ?: 0f) + dynInt
                groupedStatIntensity[label] = (groupedStatIntensity[label] ?: 0f) + statInt
            }

            val timePoints = mutableListOf<ChartPoint>()
            val intensityPoints = mutableListOf<ChartPoint>()

            groupedTime.keys.forEach { label ->
                timePoints.add(ChartPoint(label, dynamicValue = groupedTime[label] ?: 0f))
                intensityPoints.add(ChartPoint(
                    label,
                    dynamicValue = groupedDynIntensity[label] ?: 0f,
                    staticValue = groupedStatIntensity[label] ?: 0f
                ))
            }

            _timeChartData.value = timePoints
            _intensityChartData.value = intensityPoints
        }
    }

    fun updateProfile(weight: Double, height: Double) {
        viewModelScope.launch {
            val currentUser = _user.value ?: UserEntity(weightKg = weight, heightCm = height)
            val updatedUser = currentUser.copy(weightKg = weight, heightCm = height)
            db.userDao().insertOrUpdateUser(updatedUser)
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            db.weightDao().insertWeight(WeightHistoryEntity(date = currentDate, weightKg = weight))
            _user.value = updatedUser
        }
    }

    fun openWorkoutSetup() {
        _workoutState.value = WorkoutState.SETUP
        _selectedExercises.value = emptyList()
    }

    fun cancelWorkoutSetup() {
        _workoutState.value = WorkoutState.IDLE
    }

    fun toggleExerciseForSetup(exercise: ExerciseEntity) {
        val current = _selectedExercises.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.exercise.id == exercise.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            current.add(ExerciseSetup(exercise = exercise))
        }
        _selectedExercises.value = current
    }

    fun updateExerciseSetup(exerciseId: Long, reps: Int, duration: Int, distance: Double, weight: Double) {
        val current = _selectedExercises.value.toMutableList()
        val index = current.indexOfFirst { it.exercise.id == exerciseId }
        if (index >= 0) {
            current[index] = current[index].copy(
                targetReps = reps,
                targetDurationSec = duration,
                targetDistanceM = distance,
                additionalWeight = weight
            )
            _selectedExercises.value = current
        }
    }

    fun startWorkout() {
        val exercisesToDo = _selectedExercises.value
        if (exercisesToDo.isEmpty()) return

        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val startedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val newWorkout = WorkoutEntity(date = currentDate, status = "ACTIVE", startedAt = startedAt)
            val workoutId = db.workoutDao().insertWorkout(newWorkout)

            val workoutExercises = mutableListOf<WorkoutExerciseEntity>()
            exercisesToDo.forEachIndexed { index, setup ->
                val we = WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseId = setup.exercise.id,
                    orderIndex = index
                )
                val weId = db.workoutDao().insertWorkoutExercise(we)
                workoutExercises.add(we.copy(id = weId))
            }

            _activeWorkoutId.value = workoutId
            _activeWorkoutExercises.value = workoutExercises
            _currentExerciseIndex.value = 0
            _workoutState.value = WorkoutState.ACTIVE
            _isResting.value = false
            startWorkoutTimer()
        }
    }

    private fun startWorkoutTimer() {
        timerJob?.cancel()
        _workoutTimerSec.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _workoutTimerSec.value += 1
            }
        }
    }

    fun setCurrentExercise(index: Int) {
        _currentExerciseIndex.value = index
    }

    fun adjustRestDuration(delta: Int) {
        val newDuration = (_restDurationSec.value + delta).coerceAtLeast(15)
        _restDurationSec.value = newDuration

        if (_isResting.value) {
            _restTimerSec.value = (_restTimerSec.value + delta).coerceAtLeast(0)
            if (_restTimerSec.value == 0) _isResting.value = false
        }
    }

    fun startSet() {
        restJob?.cancel()
        _isResting.value = false
        _isSetRunning.value = true
        _setTimerSec.value = 0

        setJob?.cancel()
        setJob = viewModelScope.launch {
            val currentSetup = _selectedExercises.value.getOrNull(_currentExerciseIndex.value)
            val isTimed = currentSetup?.exercise?.type == "TIMED"
            val targetDuration = currentSetup?.targetDurationSec ?: 0

            while (_isSetRunning.value) {
                delay(1000)
                _setTimerSec.value += 1

                if (isTimed && targetDuration > 0 && _setTimerSec.value >= targetDuration) {
                    completeSet(null, _setTimerSec.value, null)
                    break
                }
            }
        }
    }

    fun completeSet(reps: Int?, duration: Int?, distance: Double?) {
        _isSetRunning.value = false
        setJob?.cancel()

        val currentIndex = _currentExerciseIndex.value
        val currentWe = _activeWorkoutExercises.value.getOrNull(currentIndex) ?: return
        val currentSetup = _selectedExercises.value.getOrNull(currentIndex) ?: return

        viewModelScope.launch {
            val userWeight = if (currentSetup.exercise.type in listOf("BODYWEIGHT", "TIMED")) (_user.value?.weightKg ?: 0.0) else 0.0
            val totalWeight = userWeight + currentSetup.additionalWeight
            val restTime = _restDurationSec.value
            val finalDuration = if (currentSetup.exercise.type == "TIMED") _setTimerSec.value else duration

            val newSet = WorkoutSetEntity(
                workoutExerciseId = currentWe.id,
                setIndex = 1,
                reps = reps,
                weightKg = totalWeight,
                durationSec = finalDuration,
                distanceM = distance,
                restSec = restTime,
                completed = true
            )
            db.workoutDao().insertWorkoutSet(newSet)
            startRestTimer(restTime)
        }
    }

    private fun startRestTimer(seconds: Int) {
        restJob?.cancel()
        _isResting.value = true
        _restTimerSec.value = seconds
        restJob = viewModelScope.launch {
            while (_restTimerSec.value > 0) {
                delay(1000)
                _restTimerSec.value -= 1
            }
            _isResting.value = false
        }
    }

    fun skipRest() {
        restJob?.cancel()
        _restTimerSec.value = 0
        _isResting.value = false
    }

    fun finishWorkout(difficulty: Int) {
        val workoutId = _activeWorkoutId.value ?: return
        timerJob?.cancel()
        restJob?.cancel()
        setJob?.cancel()
        viewModelScope.launch {
            val finishedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val volume = db.workoutDao().getWorkoutVolume(workoutId) ?: 0.0

            val workout = WorkoutEntity(
                id = workoutId,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                status = "FINISHED",
                difficulty = difficulty,
                totalDurationSec = _workoutTimerSec.value,
                totalVolume = volume,
                startedAt = null,
                finishedAt = finishedAt
            )
            db.workoutDao().updateWorkout(workout)

            _workoutHistory.value = db.workoutDao().getFinishedWorkouts()
            updateAnalyticsData()

            _workoutState.value = WorkoutState.IDLE
            _activeWorkoutId.value = null
            _activeWorkoutExercises.value = emptyList()
            _isResting.value = false
            _isSetRunning.value = false
        }
    }
}