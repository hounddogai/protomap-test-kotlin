package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching record is in its life cycle. */
enum class CoachingRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A coaching record as the coaching team keeps it. */
data class CoachingRecord(
    var code: String,
    var label: String,
    var rate: Double,
    var retries: Int,
    var ratio: Double,
    var reviewer: String,
    var loadKg: Double,
    var windowDays: Int,
    var vehicleId: String,
    var status: CoachingRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingRecordStatus): Boolean {
        when (status) {
            CoachingRecordStatus.PLANNED -> return next == CoachingRecordStatus.IN_PROGRESS
            CoachingRecordStatus.SCHEDULED -> {
                when (next) {
                    CoachingRecordStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingRecordStatus.IN_PROGRESS -> return next == CoachingRecordStatus.COMPLETED
            CoachingRecordStatus.COMPLETED -> return next == CoachingRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CoachingRecordStatus) {
        if (!canMoveTo(next)) {
            error("coaching record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((loadKg + retries.toDouble()) * 100) / 100

    fun snapshot(): CoachingRecord {
        return CoachingRecord(
            code = code,
            label = label,
            rate = rate,
            retries = retries,
            ratio = ratio,
            reviewer = reviewer,
            loadKg = loadKg,
            windowDays = windowDays,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a coaching record in status planned. */
fun newCoachingRecord(code: String, label: String): CoachingRecord {
    return CoachingRecord(
        code = code,
        label = label,
        rate = 0.0,
        retries = 0,
        ratio = 0.0,
        reviewer = "",
        loadKg = 0.0,
        windowDays = 0,
        vehicleId = "",
        status = CoachingRecordStatus.PLANNED,
    )
}
