package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge threshold is in its life cycle. */
enum class FuelSurchargeThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fuel surcharge threshold, with what the planners need to act on it. */
data class FuelSurchargeThreshold(
    var code: String,
    var title: String,
    var contactEmail: String,
    var currency: String,
    var priority: Int,
    var status: FuelSurchargeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeThresholdStatus): Boolean {
        when (status) {
            FuelSurchargeThresholdStatus.OPEN -> return next == FuelSurchargeThresholdStatus.RESOLVED
            FuelSurchargeThresholdStatus.IN_REVIEW -> return next == FuelSurchargeThresholdStatus.RESOLVED
            FuelSurchargeThresholdStatus.RESOLVED -> {
                when (next) {
                    FuelSurchargeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeThresholdStatus.REOPENED -> return next == FuelSurchargeThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): FuelSurchargeThreshold {
        return FuelSurchargeThreshold(
            code = code,
            title = title,
            contactEmail = contactEmail,
            currency = currency,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge threshold in status open. */
fun newFuelSurchargeThreshold(code: String, title: String): FuelSurchargeThreshold {
    return FuelSurchargeThreshold(
        code = code,
        title = title,
        contactEmail = "",
        currency = "",
        priority = 0,
        status = FuelSurchargeThresholdStatus.OPEN,
    )
}
