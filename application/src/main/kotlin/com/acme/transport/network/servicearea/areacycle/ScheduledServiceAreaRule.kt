package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.kit.text.normalize

/** Where a scheduled service area rule is in its life cycle. */
enum class ScheduledServiceAreaRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled service area rule as the service area team keeps it. */
data class ScheduledServiceAreaRule(
    var code: String,
    var name: String,
    var reviewer: String,
    var notes: String,
    var minimum: Double,
    var status: ScheduledServiceAreaRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServiceAreaRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServiceAreaRuleStatus): Boolean {
        when (status) {
            ScheduledServiceAreaRuleStatus.PLANNED -> return next == ScheduledServiceAreaRuleStatus.COMPLETED
            ScheduledServiceAreaRuleStatus.SCHEDULED -> {
                when (next) {
                    ScheduledServiceAreaRuleStatus.IN_PROGRESS, ScheduledServiceAreaRuleStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledServiceAreaRuleStatus.IN_PROGRESS -> return next == ScheduledServiceAreaRuleStatus.COMPLETED || next == ScheduledServiceAreaRuleStatus.CANCELLED
            ScheduledServiceAreaRuleStatus.COMPLETED -> {
                when (next) {
                    ScheduledServiceAreaRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledServiceAreaRuleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service area rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled service area rule"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): ScheduledServiceAreaRule {
        return ScheduledServiceAreaRule(
            code = code,
            name = name,
            reviewer = reviewer,
            notes = notes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled service area rule in status planned. */
fun newScheduledServiceAreaRule(code: String, name: String): ScheduledServiceAreaRule {
    return ScheduledServiceAreaRule(
        code = code,
        name = name,
        reviewer = "",
        notes = "",
        minimum = 0.0,
        status = ScheduledServiceAreaRuleStatus.PLANNED,
    )
}
