package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional service area budget is in its life cycle. */
enum class RegionalServiceAreaBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional service area budget as the service area team keeps it. */
data class RegionalServiceAreaBudget(
    var code: String,
    var label: String,
    var weight: Double,
    var sequence: Int,
    var ratio: Double,
    var status: RegionalServiceAreaBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaBudgetStatus): Boolean {
        when (status) {
            RegionalServiceAreaBudgetStatus.NEW -> return next == RegionalServiceAreaBudgetStatus.QUEUED || next == RegionalServiceAreaBudgetStatus.DONE
            RegionalServiceAreaBudgetStatus.QUEUED -> {
                when (next) {
                    RegionalServiceAreaBudgetStatus.DONE, RegionalServiceAreaBudgetStatus.FAILED -> return true
                }
                return false
            }
            RegionalServiceAreaBudgetStatus.RUNNING -> {
                when (next) {
                    RegionalServiceAreaBudgetStatus.DONE, RegionalServiceAreaBudgetStatus.FAILED -> return true
                }
                return false
            }
            RegionalServiceAreaBudgetStatus.DONE -> {
                when (next) {
                    RegionalServiceAreaBudgetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional service area budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((weight - sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalServiceAreaBudget {
        return RegionalServiceAreaBudget(
            code = code,
            label = label,
            weight = weight,
            sequence = sequence,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional service area budget in status new. */
fun newRegionalServiceAreaBudget(code: String, label: String): RegionalServiceAreaBudget {
    return RegionalServiceAreaBudget(
        code = code,
        label = label,
        weight = 0.0,
        sequence = 0,
        ratio = 0.0,
        status = RegionalServiceAreaBudgetStatus.NEW,
    )
}
