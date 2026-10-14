package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict zone schedule is in its life cycle. */
enum class StrictZoneScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict zone schedule and its current status. */
data class StrictZoneSchedule(
    var code: String,
    var label: String,
    var notes: String,
    var createdAt: Int,
    var region: String,
    var discount: Double,
    var reviewer: String,
    var loadKg: Double,
    var status: StrictZoneScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictZoneScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictZoneScheduleStatus): Boolean {
        when (status) {
            StrictZoneScheduleStatus.DRAFT -> return next == StrictZoneScheduleStatus.CLOSED
            StrictZoneScheduleStatus.PENDING -> return next == StrictZoneScheduleStatus.ACTIVE
            StrictZoneScheduleStatus.ACTIVE -> {
                when (next) {
                    StrictZoneScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictZoneScheduleStatus.SUSPENDED -> {
                when (next) {
                    StrictZoneScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictZoneScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict zone schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict zone schedule"} $code"

    fun weightedValue(): Double = round((discount - createdAt.toDouble()) * 100) / 100

    fun snapshot(): StrictZoneSchedule {
        return StrictZoneSchedule(
            code = code,
            label = label,
            notes = notes,
            createdAt = createdAt,
            region = region,
            discount = discount,
            reviewer = reviewer,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict zone schedule in status draft. */
fun newStrictZoneSchedule(code: String, label: String): StrictZoneSchedule {
    return StrictZoneSchedule(
        code = code,
        label = label,
        notes = "",
        createdAt = 0,
        region = "",
        discount = 0.0,
        reviewer = "",
        loadKg = 0.0,
        status = StrictZoneScheduleStatus.DRAFT,
    )
}
