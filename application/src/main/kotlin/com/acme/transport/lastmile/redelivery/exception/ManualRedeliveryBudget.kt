package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual redelivery budget is in its life cycle. */
enum class ManualRedeliveryBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual redelivery budget and its current status. */
data class ManualRedeliveryBudget(
    var code: String,
    var label: String,
    var externalRef: String,
    var cost: Double,
    var source: String,
    var visible: Boolean,
    var fuelLiters: Double,
    var routeCode: String,
    var status: ManualRedeliveryBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRedeliveryBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRedeliveryBudgetStatus): Boolean {
        when (status) {
            ManualRedeliveryBudgetStatus.CREATED -> return next == ManualRedeliveryBudgetStatus.VALIDATED || next == ManualRedeliveryBudgetStatus.PUBLISHED
            ManualRedeliveryBudgetStatus.VALIDATED -> {
                when (next) {
                    ManualRedeliveryBudgetStatus.PUBLISHED, ManualRedeliveryBudgetStatus.RETIRED -> return true
                }
                return false
            }
            ManualRedeliveryBudgetStatus.PUBLISHED -> {
                when (next) {
                    ManualRedeliveryBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRedeliveryBudgetStatus) {
        if (!canMoveTo(next)) {
            error("manual redelivery budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round((fuelLiters - cost) * 100) / 100

    fun snapshot(): ManualRedeliveryBudget {
        return ManualRedeliveryBudget(
            code = code,
            label = label,
            externalRef = externalRef,
            cost = cost,
            source = source,
            visible = visible,
            fuelLiters = fuelLiters,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual redelivery budget in status created. */
fun newManualRedeliveryBudget(code: String, label: String): ManualRedeliveryBudget {
    return ManualRedeliveryBudget(
        code = code,
        label = label,
        externalRef = "",
        cost = 0.0,
        source = "",
        visible = false,
        fuelLiters = 0.0,
        routeCode = "",
        status = ManualRedeliveryBudgetStatus.CREATED,
    )
}
