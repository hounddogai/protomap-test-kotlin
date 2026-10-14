package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard slot records of safety's scorecard work, and the rules, stores, and calculations around them.

/** Where a scorecard slot is in its life cycle. */
enum class ScorecardSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scorecard slot, with what the planners need to act on it. */
data class ScorecardSlot(
    var code: String,
    var name: String,
    var label: String,
    var floorValue: Double,
    var archived: Boolean,
    var startsAt: Int,
    var revision: Int,
    var dock: String,
    var attempts: Int,
    var batchSize: Int,
    var odometer: Int,
    var status: ScorecardSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardSlotStatus): Boolean {
        when (status) {
            ScorecardSlotStatus.NEW -> return next == ScorecardSlotStatus.QUEUED || next == ScorecardSlotStatus.DONE
            ScorecardSlotStatus.QUEUED -> {
                when (next) {
                    ScorecardSlotStatus.RUNNING -> return true
                }
                return false
            }
            ScorecardSlotStatus.RUNNING -> {
                when (next) {
                    ScorecardSlotStatus.DONE -> return true
                }
                return false
            }
            ScorecardSlotStatus.DONE -> {
                when (next) {
                    ScorecardSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardSlotStatus) {
        if (!canMoveTo(next)) {
            error("scorecard slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((revision.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ScorecardSlot {
        return ScorecardSlot(
            code = code,
            name = name,
            label = label,
            floorValue = floorValue,
            archived = archived,
            startsAt = startsAt,
            revision = revision,
            dock = dock,
            attempts = attempts,
            batchSize = batchSize,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scorecard slot in status new. */
fun newScorecardSlot(code: String, name: String): ScorecardSlot {
    return ScorecardSlot(
        code = code,
        name = name,
        label = "",
        floorValue = 0.0,
        archived = false,
        startsAt = 0,
        revision = 0,
        dock = "",
        attempts = 0,
        batchSize = 0,
        odometer = 0,
        status = ScorecardSlotStatus.NEW,
    )
}
