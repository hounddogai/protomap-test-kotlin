package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card checkpoint is in its life cycle. */
enum class RateCardCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rate card checkpoint as the rate card team keeps it. */
data class RateCardCheckpoint(
    var code: String,
    var label: String,
    var currency: String,
    var driverId: String,
    var timezone: String,
    var createdAt: Int,
    var pallets: Int,
    var discount: Double,
    var dock: String,
    var status: RateCardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardCheckpointStatus): Boolean {
        when (status) {
            RateCardCheckpointStatus.PROPOSED -> {
                when (next) {
                    RateCardCheckpointStatus.APPROVED, RateCardCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            RateCardCheckpointStatus.APPROVED -> return next == RateCardCheckpointStatus.REJECTED
            RateCardCheckpointStatus.REJECTED -> {
                when (next) {
                    RateCardCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rate card checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((pallets.toDouble() - discount) * 100) / 100

    fun snapshot(): RateCardCheckpoint {
        return RateCardCheckpoint(
            code = code,
            label = label,
            currency = currency,
            driverId = driverId,
            timezone = timezone,
            createdAt = createdAt,
            pallets = pallets,
            discount = discount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rate card checkpoint in status proposed. */
fun newRateCardCheckpoint(code: String, label: String): RateCardCheckpoint {
    return RateCardCheckpoint(
        code = code,
        label = label,
        currency = "",
        driverId = "",
        timezone = "",
        createdAt = 0,
        pallets = 0,
        discount = 0.0,
        dock = "",
        status = RateCardCheckpointStatus.PROPOSED,
    )
}
