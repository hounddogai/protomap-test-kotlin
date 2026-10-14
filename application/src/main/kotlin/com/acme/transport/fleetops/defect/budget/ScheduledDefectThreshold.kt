package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled defect threshold is in its life cycle. */
enum class ScheduledDefectThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled defect threshold, with what the planners need to act on it. */
data class ScheduledDefectThreshold(
    var code: String,
    var name: String,
    var contactEmail: String,
    var budget: Double,
    var channel: String,
    var graceDays: Int,
    var status: ScheduledDefectThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectThresholdStatus): Boolean {
        when (status) {
            ScheduledDefectThresholdStatus.DRAFT -> {
                when (next) {
                    ScheduledDefectThresholdStatus.ACTIVE, ScheduledDefectThresholdStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledDefectThresholdStatus.PENDING -> {
                when (next) {
                    ScheduledDefectThresholdStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledDefectThresholdStatus.ACTIVE -> {
                when (next) {
                    ScheduledDefectThresholdStatus.SUSPENDED, ScheduledDefectThresholdStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledDefectThresholdStatus.SUSPENDED -> return next == ScheduledDefectThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledDefectThresholdStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(budget * graceDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledDefectThreshold {
        return ScheduledDefectThreshold(
            code = code,
            name = name,
            contactEmail = contactEmail,
            budget = budget,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled defect threshold in status draft. */
fun newScheduledDefectThreshold(code: String, name: String): ScheduledDefectThreshold {
    return ScheduledDefectThreshold(
        code = code,
        name = name,
        contactEmail = "",
        budget = 0.0,
        channel = "",
        graceDays = 0,
        status = ScheduledDefectThresholdStatus.DRAFT,
    )
}
