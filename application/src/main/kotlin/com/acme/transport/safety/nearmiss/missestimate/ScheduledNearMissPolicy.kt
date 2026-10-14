package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled near miss policy is in its life cycle. */
enum class ScheduledNearMissPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One scheduled near miss policy, with what the planners need to act on it. */
data class ScheduledNearMissPolicy(
    var code: String,
    var label: String,
    var visible: Boolean,
    var rate: Double,
    var discount: Double,
    var priority: Int,
    var retries: Int,
    var startsAt: Int,
    var maximum: Double,
    var budget: Double,
    var summary: String,
    var status: ScheduledNearMissPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissPolicyStatus): Boolean {
        when (status) {
            ScheduledNearMissPolicyStatus.PLANNED -> return next == ScheduledNearMissPolicyStatus.IN_PROGRESS || next == ScheduledNearMissPolicyStatus.CANCELLED
            ScheduledNearMissPolicyStatus.SCHEDULED -> return next == ScheduledNearMissPolicyStatus.COMPLETED || next == ScheduledNearMissPolicyStatus.CANCELLED
            ScheduledNearMissPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledNearMissPolicyStatus.COMPLETED, ScheduledNearMissPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledNearMissPolicyStatus.COMPLETED -> return next == ScheduledNearMissPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round((retries.toDouble() + maximum) * 100) / 100

    fun snapshot(): ScheduledNearMissPolicy {
        return ScheduledNearMissPolicy(
            code = code,
            label = label,
            visible = visible,
            rate = rate,
            discount = discount,
            priority = priority,
            retries = retries,
            startsAt = startsAt,
            maximum = maximum,
            budget = budget,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled near miss policy in status planned. */
fun newScheduledNearMissPolicy(code: String, label: String): ScheduledNearMissPolicy {
    return ScheduledNearMissPolicy(
        code = code,
        label = label,
        visible = false,
        rate = 0.0,
        discount = 0.0,
        priority = 0,
        retries = 0,
        startsAt = 0,
        maximum = 0.0,
        budget = 0.0,
        summary = "",
        status = ScheduledNearMissPolicyStatus.PLANNED,
    )
}
