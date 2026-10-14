package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period rule is in its life cycle. */
enum class DutyPeriodRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One duty period rule, with what the planners need to act on it. */
data class DutyPeriodRule(
    var code: String,
    var label: String,
    var odometer: Int,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var revision: Int,
    var expiresAt: Int,
    var status: DutyPeriodRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodRuleStatus): Boolean {
        when (status) {
            DutyPeriodRuleStatus.PLANNED -> return next == DutyPeriodRuleStatus.CANCELLED
            DutyPeriodRuleStatus.SCHEDULED -> {
                when (next) {
                    DutyPeriodRuleStatus.COMPLETED -> return true
                }
                return false
            }
            DutyPeriodRuleStatus.IN_PROGRESS -> return next == DutyPeriodRuleStatus.COMPLETED
            DutyPeriodRuleStatus.COMPLETED -> {
                when (next) {
                    DutyPeriodRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodRuleStatus) {
        if (!canMoveTo(next)) {
            error("duty period rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"duty period rule"} $code"

    fun adjustedValue(): Double = round((expiresAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodRule {
        return DutyPeriodRule(
            code = code,
            label = label,
            odometer = odometer,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            revision = revision,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period rule in status planned. */
fun newDutyPeriodRule(code: String, label: String): DutyPeriodRule {
    return DutyPeriodRule(
        code = code,
        label = label,
        odometer = 0,
        fuelLiters = 0.0,
        durationMinutes = 0,
        revision = 0,
        expiresAt = 0,
        status = DutyPeriodRuleStatus.PLANNED,
    )
}
