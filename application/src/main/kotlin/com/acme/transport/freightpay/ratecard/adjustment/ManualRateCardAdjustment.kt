package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.normalize

// The manual rate card adjustment records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a manual rate card adjustment is in its life cycle. */
enum class ManualRateCardAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a manual rate card adjustment and its current status. */
data class ManualRateCardAdjustment(
    var code: String,
    var title: String,
    var reviewer: String,
    var discount: Double,
    var vehicleId: String,
    var status: ManualRateCardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRateCardAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRateCardAdjustmentStatus): Boolean {
        when (status) {
            ManualRateCardAdjustmentStatus.PROPOSED -> {
                when (next) {
                    ManualRateCardAdjustmentStatus.APPROVED -> return true
                }
                return false
            }
            ManualRateCardAdjustmentStatus.APPROVED -> {
                when (next) {
                    ManualRateCardAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualRateCardAdjustmentStatus.REJECTED -> {
                when (next) {
                    ManualRateCardAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRateCardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("manual rate card adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual rate card adjustment"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): ManualRateCardAdjustment {
        return ManualRateCardAdjustment(
            code = code,
            title = title,
            reviewer = reviewer,
            discount = discount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual rate card adjustment in status proposed. */
fun newManualRateCardAdjustment(code: String, title: String): ManualRateCardAdjustment {
    return ManualRateCardAdjustment(
        code = code,
        title = title,
        reviewer = "",
        discount = 0.0,
        vehicleId = "",
        status = ManualRateCardAdjustmentStatus.PROPOSED,
    )
}
