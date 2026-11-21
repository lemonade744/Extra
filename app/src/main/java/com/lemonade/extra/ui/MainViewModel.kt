package com.lemonade.extra.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lemonade.extra.data.database.AppDatabase
import com.lemonade.extra.data.entity.ExerciseEntity
import com.lemonade.extra.data.entity.WorkoutEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _exercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    val exercises: StateFlow<List<ExerciseEntity>> = _exercises

    // НОВОЕ: Состояние активной тренировки
    private val _activeWorkoutId = MutableStateFlow<Long?>(null)
    val activeWorkoutId: StateFlow<Long?> = _activeWorkoutId

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _exercises.value = db.exerciseDao().getAllExercises()
        }
    }

    fun startWorkout() {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newWorkout = WorkoutEntity(date = currentDate, status = "ACTIVE")
            val workoutId = db.workoutDao().insertWorkout(newWorkout)

            // Говорим экрану, что тренировка началась
            _activeWorkoutId.value = workoutId
            Log.d("WORKOUT_TEST", "Создана тренировка с ID: $workoutId")
        }
    }

    fun finishWorkout() {
        val workoutId = _activeWorkoutId.value ?: return
        viewModelScope.launch {
            db.workoutDao().finishWorkout(workoutId)
            _activeWorkoutId.value = null // Возвращаемся на главный экран
            Log.d("WORKOUT_TEST", "Тренировка $workoutId завершена")
        }
    }
}