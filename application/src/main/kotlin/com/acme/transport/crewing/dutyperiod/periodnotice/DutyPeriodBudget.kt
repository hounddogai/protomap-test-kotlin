package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period budget is in its life cycle. */
enum class DutyPeriodBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a duty period budget and its current status. */
data class DutyPeriodBudget(
    var code: String,
    var label: String,
    var rate: Double,
    var expiresAt: Int,
    var quantity: Int,
    var odometer: Int,
    var discount: Double,
    var status: DutyPeriodBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodBudgetStatus): Boolean {
        when (status) {
            DutyPeriodBudgetStatus.CREATED -> {
                when (next) {
                    DutyPeriodBudgetStatus.PUBLISHED -> return true
                }
                return false
            }
            DutyPeriodBudgetStatus.VALIDATED -> {
                when (next) {
                    DutyPeriodBudgetStatus.PUBLISHED, DutyPeriodBudgetStatus.RETIRED -> return true
                }
                return false
            }
            DutyPeriodBudgetStatus.PUBLISHED -> {
                when (next) {
                    DutyPeriodBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodBudgetStatus) {
        if (!canMoveTo(next)) {
            error("duty period budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"duty period budget"} $code"

    fun adjustedVolume(): Double = round((discount + odometer.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodBudget {
        return DutyPeriodBudget(
            code = code,
            label = label,
            rate = rate,
            expiresAt = expiresAt,
            quantity = quantity,
            odometer = odometer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period budget in status created. */
fun newDutyPeriodBudget(code: String, label: String): DutyPeriodBudget {
    return DutyPeriodBudget(
        code = code,
        label = label,
        rate = 0.0,
        expiresAt = 0,
        quantity = 0,
        odometer = 0,
        discount = 0.0,
        status = DutyPeriodBudgetStatus.CREATED,
    )
}
