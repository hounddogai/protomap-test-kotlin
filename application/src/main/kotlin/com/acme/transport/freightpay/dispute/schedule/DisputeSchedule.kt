package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute schedule records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute schedule is in its life cycle. */
enum class DisputeScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dispute schedule as the dispute team keeps it. */
data class DisputeSchedule(
    var code: String,
    var label: String,
    var vehicleId: String,
    var updatedAt: Int,
    var retries: Int,
    var shiftCode: String,
    var maximum: Double,
    var attempts: Int,
    var tags: MutableList<String>,
    var attributes: MutableMap<String, String>,
    var status: DisputeScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeScheduleStatus): Boolean {
        when (status) {
            DisputeScheduleStatus.PLANNED -> return next == DisputeScheduleStatus.CANCELLED
            DisputeScheduleStatus.SCHEDULED -> {
                when (next) {
                    DisputeScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            DisputeScheduleStatus.IN_PROGRESS -> return next == DisputeScheduleStatus.CANCELLED
            DisputeScheduleStatus.COMPLETED -> return next == DisputeScheduleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DisputeScheduleStatus) {
        if (!canMoveTo(next)) {
            error("dispute schedule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((updatedAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): DisputeSchedule {
        return DisputeSchedule(
            code = code,
            label = label,
            vehicleId = vehicleId,
            updatedAt = updatedAt,
            retries = retries,
            shiftCode = shiftCode,
            maximum = maximum,
            attempts = attempts,
            tags = tags,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dispute schedule in status planned. */
fun newDisputeSchedule(code: String, label: String): DisputeSchedule {
    return DisputeSchedule(
        code = code,
        label = label,
        vehicleId = "",
        updatedAt = 0,
        retries = 0,
        shiftCode = "",
        maximum = 0.0,
        attempts = 0,
        tags = mutableListOf<String>(),
        attributes = mutableMapOf<String, String>(),
        status = DisputeScheduleStatus.PLANNED,
    )
}
