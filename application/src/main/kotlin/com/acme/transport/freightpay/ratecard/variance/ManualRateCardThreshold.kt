package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual rate card threshold is in its life cycle. */
enum class ManualRateCardThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual rate card threshold and its current status. */
data class ManualRateCardThreshold(
    var code: String,
    var name: String,
    var pallets: Int,
    var checksum: String,
    var quantity: Int,
    var budget: Double,
    var status: ManualRateCardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRateCardThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRateCardThresholdStatus): Boolean {
        when (status) {
            ManualRateCardThresholdStatus.OPEN -> return next == ManualRateCardThresholdStatus.IN_REVIEW
            ManualRateCardThresholdStatus.IN_REVIEW -> {
                when (next) {
                    ManualRateCardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualRateCardThresholdStatus.RESOLVED -> {
                when (next) {
                    ManualRateCardThresholdStatus.REOPENED -> return true
                }
                return false
            }
            ManualRateCardThresholdStatus.REOPENED -> {
                when (next) {
                    ManualRateCardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRateCardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("manual rate card threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual rate card threshold"} $code"

    fun weightedValue(): Double = round(quantity.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): ManualRateCardThreshold {
        return ManualRateCardThreshold(
            code = code,
            name = name,
            pallets = pallets,
            checksum = checksum,
            quantity = quantity,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual rate card threshold in status open. */
fun newManualRateCardThreshold(code: String, name: String): ManualRateCardThreshold {
    return ManualRateCardThreshold(
        code = code,
        name = name,
        pallets = 0,
        checksum = "",
        quantity = 0,
        budget = 0.0,
        status = ManualRateCardThresholdStatus.OPEN,
    )
}
