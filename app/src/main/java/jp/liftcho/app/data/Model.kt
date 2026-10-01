package jp.liftcho.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val dayKey: String,
    val orderIdx: Int,
    val sets: Int,
    val targetWeight: Double?,
    val targetReps: Int,
    val repMin: Int,
    val repMax: Int,
    val step: Double,
    val restSec: Int,
    val changeNote: String = ""
)

@Entity(tableName = "set_logs", primaryKeys = ["date", "exerciseId", "setNo"])
data class SetLog(
    val date: String,
    val dayKey: String,
    val exerciseId: String,
    val setNo: Int,
    val weight: Double?,
    val reps: Int,
    val done: Boolean
)

data class DaySummary(
    val date: String,
    val dayKey: String,
    val doneSets: Int,
    val volume: Double
)

@Dao
interface LiftDao {
    @Query("SELECT * FROM exercises WHERE dayKey = :dayKey ORDER BY orderIdx")
    fun exercises(dayKey: String): Flow<List<Exercise>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun exerciseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(items: List<Exercise>)

    @Query("SELECT * FROM set_logs WHERE date = :date AND dayKey = :dayKey ORDER BY exerciseId, setNo")
    fun logs(date: String, dayKey: String): Flow<List<SetLog>>

    @Query("SELECT COUNT(*) FROM set_logs WHERE date = :date AND dayKey = :dayKey")
    suspend fun logCount(date: String, dayKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(items: List<SetLog>)

    @Update
    suspend fun updateLog(log: SetLog)

    @Query(
        "SELECT * FROM set_logs WHERE exerciseId = :exerciseId AND done = 1 AND date < :before " +
            "ORDER BY date DESC, setNo ASC"
    )
    suspend fun previousSets(exerciseId: String, before: String): List<SetLog>

    @Query(
        "SELECT date AS date, dayKey AS dayKey, COUNT(*) AS doneSets, " +
            "SUM(COALESCE(weight, 0) * reps) AS volume FROM set_logs WHERE done = 1 " +
            "GROUP BY date, dayKey ORDER BY date DESC LIMIT 60"
    )
    fun history(): Flow<List<DaySummary>>

    @Query("SELECT * FROM set_logs WHERE date = :date AND done = 1 ORDER BY exerciseId, setNo")
    suspend fun doneSetsOf(date: String): List<SetLog>

    @Query(
        "DELETE FROM set_logs WHERE date = :date AND dayKey = :dayKey " +
            "AND exerciseId = :exerciseId AND setNo = :setNo"
    )
    suspend fun deleteSet(date: String, dayKey: String, exerciseId: String, setNo: Int)
}

@Database(entities = [Exercise::class, SetLog::class], version = 1, exportSchema = true)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): LiftDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun get(context: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java,
                "liftcho.db"
            ).build().also { instance = it }
        }
    }
}

class Repo(private val dao: LiftDao) {

    fun exercises(dayKey: String): Flow<List<Exercise>> = dao.exercises(dayKey)

    fun logs(date: String, dayKey: String): Flow<List<SetLog>> = dao.logs(date, dayKey)

    fun history(): Flow<List<DaySummary>> = dao.history()

    suspend fun seedIfEmpty() {
        if (dao.exerciseCount() == 0) dao.insertExercises(Seed.exercises)
    }

    /** その日のセット行がまだ無ければ、メニューの目標値で作る */
    suspend fun ensureSession(date: String, dayKey: String) {
        if (dao.logCount(date, dayKey) > 0) return
        val menu = dao.exercises(dayKey).first()
        if (menu.isEmpty()) return
        val rows = menu.flatMap { e ->
            (1..e.sets).map { n ->
                SetLog(date, dayKey, e.id, n, e.targetWeight, e.targetReps, false)
            }
        }
        dao.insertLogs(rows)
    }

    suspend fun toggleDone(log: SetLog) = dao.updateLog(log.copy(done = !log.done))

    suspend fun setWeight(log: SetLog, weight: Double?) = dao.updateLog(log.copy(weight = weight))

    suspend fun setReps(log: SetLog, reps: Int) = dao.updateLog(log.copy(reps = reps))

    /** 未完了の後続セットにも同じ重量を流す */
    suspend fun spreadWeight(all: List<SetLog>, from: SetLog, weight: Double?) {
        all.filter { it.exerciseId == from.exerciseId && it.setNo >= from.setNo && (!it.done || it.setNo == from.setNo) }
            .forEach { dao.updateLog(it.copy(weight = weight)) }
    }

    suspend fun addSet(exercise: Exercise, existing: List<SetLog>) {
        val rows = existing.filter { it.exerciseId == exercise.id }
        val last = rows.maxByOrNull { it.setNo } ?: return
        dao.insertLogs(listOf(last.copy(setNo = last.setNo + 1, done = false)))
    }

    /** 最後のセットを1つ削除する。記録済み（✓）のセットは消さない */
    suspend fun removeLastSet(rows: List<SetLog>, exerciseId: String) {
        val target = rows.filter { it.exerciseId == exerciseId }.maxByOrNull { it.setNo } ?: return
        if (target.done) return
        if (rows.count { it.exerciseId == exerciseId } <= 1) return
        dao.deleteSet(target.date, target.dayKey, target.exerciseId, target.setNo)
    }

    suspend fun previousSession(exerciseId: String, before: String): List<SetLog> {
        val all = dao.previousSets(exerciseId, before)
        val latest = all.firstOrNull()?.date ?: return emptyList()
        return all.filter { it.date == latest }
    }

    suspend fun doneSetsOf(date: String): List<SetLog> = dao.doneSetsOf(date)
}
