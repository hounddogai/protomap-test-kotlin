package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient near miss schedule is in its life cycle. */
enum class LenientNearMissScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient near miss schedule as the near miss team keeps it. */
data class LenientNearMissSchedule(
    var code: String,
    var label: String,
    var driverId: String,
    var phone: String,
    var limit: Int,
    var loadKg: Double,
    var summary: String,
    var status: LenientNearMissScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNearMissScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNearMissScheduleStatus): Boolean {
        when (status) {
            LenientNearMissScheduleStatus.PLANNED -> {
                when (next) {
                    LenientNearMissScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            LenientNearMissScheduleStatus.SCHEDULED -> {
                when (next) {
                    LenientNearMissScheduleStatus.IN_PROGRESS, LenientNearMissScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            LenientNearMissScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    LenientNearMissScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            LenientNearMissScheduleStatus.COMPLETED -> {
                when (next) {
                    LenientNearMissScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientNearMissScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lenient near miss schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((loadKg - limit.toDouble()) * 100) / 100

    fun snapshot(): LenientNearMissSchedule {
        return LenientNearMissSchedule(
            code = code,
            label = label,
            driverId = driverId,
            phone = phone,
            limit = limit,
            loadKg = loadKg,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient near miss schedule in status planned. */
fun newLenientNearMissSchedule(code: String, label: String): LenientNearMissSchedule {
    return LenientNearMissSchedule(
        code = code,
        label = label,
        driverId = "",
        phone = "",
        limit = 0,
        loadKg = 0.0,
        summary = "",
        status = LenientNearMissScheduleStatus.PLANNED,
    )
}
