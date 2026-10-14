package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.kit.text.normalize

// The fuel surcharge exception records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge exception is in its life cycle. */
enum class FuelSurchargeExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel surcharge exception, with what the planners need to act on it. */
data class FuelSurchargeException(
    var code: String,
    var title: String,
    var budget: Double,
    var ownerId: String,
    var reviewer: String,
    var region: String,
    var timezone: String,
    var segment: String,
    var status: FuelSurchargeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeExceptionStatus): Boolean {
        when (status) {
            FuelSurchargeExceptionStatus.IDLE -> {
                when (next) {
                    FuelSurchargeExceptionStatus.WARMING -> return true
                }
                return false
            }
            FuelSurchargeExceptionStatus.WARMING -> return next == FuelSurchargeExceptionStatus.STOPPED
            FuelSurchargeExceptionStatus.READY -> return next == FuelSurchargeExceptionStatus.DRAINING || next == FuelSurchargeExceptionStatus.STOPPED
            FuelSurchargeExceptionStatus.DRAINING -> {
                when (next) {
                    FuelSurchargeExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = budget

    fun snapshot(): FuelSurchargeException {
        return FuelSurchargeException(
            code = code,
            title = title,
            budget = budget,
            ownerId = ownerId,
            reviewer = reviewer,
            region = region,
            timezone = timezone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel surcharge exception in status idle. */
fun newFuelSurchargeException(code: String, title: String): FuelSurchargeException {
    return FuelSurchargeException(
        code = code,
        title = title,
        budget = 0.0,
        ownerId = "",
        reviewer = "",
        region = "",
        timezone = "",
        segment = "",
        status = FuelSurchargeExceptionStatus.IDLE,
    )
}
