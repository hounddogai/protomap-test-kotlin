package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted freight invoice budget is in its life cycle. */
enum class WeightedFreightInvoiceBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted freight invoice budget and its current status. */
data class WeightedFreightInvoiceBudget(
    var code: String,
    var title: String,
    var priority: Int,
    var carrier: String,
    var updatedAt: Int,
    var capacity: Int,
    var name: String,
    var vehicleId: String,
    var routeCode: String,
    var visible: Boolean,
    var status: WeightedFreightInvoiceBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFreightInvoiceBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFreightInvoiceBudgetStatus): Boolean {
        when (status) {
            WeightedFreightInvoiceBudgetStatus.NEW -> return next == WeightedFreightInvoiceBudgetStatus.RUNNING || next == WeightedFreightInvoiceBudgetStatus.FAILED
            WeightedFreightInvoiceBudgetStatus.QUEUED -> {
                when (next) {
                    WeightedFreightInvoiceBudgetStatus.FAILED -> return true
                }
                return false
            }
            WeightedFreightInvoiceBudgetStatus.RUNNING -> {
                when (next) {
                    WeightedFreightInvoiceBudgetStatus.DONE -> return true
                }
                return false
            }
            WeightedFreightInvoiceBudgetStatus.DONE -> return next == WeightedFreightInvoiceBudgetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedFreightInvoiceBudgetStatus) {
        if (!canMoveTo(next)) {
            error("weighted freight invoice budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted freight invoice budget"} $code"

    fun weightedExposure(): Double = round((updatedAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedFreightInvoiceBudget {
        return WeightedFreightInvoiceBudget(
            code = code,
            title = title,
            priority = priority,
            carrier = carrier,
            updatedAt = updatedAt,
            capacity = capacity,
            name = name,
            vehicleId = vehicleId,
            routeCode = routeCode,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted freight invoice budget in status new. */
fun newWeightedFreightInvoiceBudget(code: String, title: String): WeightedFreightInvoiceBudget {
    return WeightedFreightInvoiceBudget(
        code = code,
        title = title,
        priority = 0,
        carrier = "",
        updatedAt = 0,
        capacity = 0,
        name = "",
        vehicleId = "",
        routeCode = "",
        visible = false,
        status = WeightedFreightInvoiceBudgetStatus.NEW,
    )
}
