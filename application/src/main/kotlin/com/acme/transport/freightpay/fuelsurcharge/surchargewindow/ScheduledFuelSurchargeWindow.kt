package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled fuel surcharge window records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a scheduled fuel surcharge window is in its life cycle. */
enum class ScheduledFuelSurchargeWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled fuel surcharge window and its current status. */
data class ScheduledFuelSurchargeWindow(
    var code: String,
    var name: String,
    var budget: Double,
    var limit: Int,
    var timezone: String,
    var label: String,
    var carrier: String,
    var stopCount: Int,
    var title: String,
    var visible: Boolean,
    var status: ScheduledFuelSurchargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelSurchargeWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelSurchargeWindowStatus): Boolean {
        when (status) {
            ScheduledFuelSurchargeWindowStatus.PROPOSED -> {
                when (next) {
                    ScheduledFuelSurchargeWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledFuelSurchargeWindowStatus.APPROVED -> return next == ScheduledFuelSurchargeWindowStatus.REJECTED
            ScheduledFuelSurchargeWindowStatus.REJECTED -> return next == ScheduledFuelSurchargeWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledFuelSurchargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel surcharge window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((limit.toDouble() - budget) * 100) / 100

    fun snapshot(): ScheduledFuelSurchargeWindow {
        return ScheduledFuelSurchargeWindow(
            code = code,
            name = name,
            budget = budget,
            limit = limit,
            timezone = timezone,
            label = label,
            carrier = carrier,
            stopCount = stopCount,
            title = title,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled fuel surcharge window in status proposed. */
fun newScheduledFuelSurchargeWindow(code: String, name: String): ScheduledFuelSurchargeWindow {
    return ScheduledFuelSurchargeWindow(
        code = code,
        name = name,
        budget = 0.0,
        limit = 0,
        timezone = "",
        label = "",
        carrier = "",
        stopCount = 0,
        title = "",
        visible = false,
        status = ScheduledFuelSurchargeWindowStatus.PROPOSED,
    )
}
