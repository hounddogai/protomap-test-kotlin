package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict rate card threshold records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a strict rate card threshold is in its life cycle. */
enum class StrictRateCardThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict rate card threshold. Its code is unique within a tenant. */
data class StrictRateCardThreshold(
    var code: String,
    var label: String,
    var driverId: String,
    var loadKg: Double,
    var depot: String,
    var attempts: Int,
    var dock: String,
    var carrier: String,
    var contactEmail: String,
    var status: StrictRateCardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardThresholdStatus): Boolean {
        when (status) {
            StrictRateCardThresholdStatus.REQUESTED -> {
                when (next) {
                    StrictRateCardThresholdStatus.GRANTED -> return true
                }
                return false
            }
            StrictRateCardThresholdStatus.GRANTED -> return next == StrictRateCardThresholdStatus.EXPIRED
            StrictRateCardThresholdStatus.REVOKED -> return next == StrictRateCardThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictRateCardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict rate card threshold"} $code"

    fun projectedExposure(): Double = round((loadKg + attempts.toDouble()) * 100) / 100

    fun snapshot(): StrictRateCardThreshold {
        return StrictRateCardThreshold(
            code = code,
            label = label,
            driverId = driverId,
            loadKg = loadKg,
            depot = depot,
            attempts = attempts,
            dock = dock,
            carrier = carrier,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict rate card threshold in status requested. */
fun newStrictRateCardThreshold(code: String, label: String): StrictRateCardThreshold {
    return StrictRateCardThreshold(
        code = code,
        label = label,
        driverId = "",
        loadKg = 0.0,
        depot = "",
        attempts = 0,
        dock = "",
        carrier = "",
        contactEmail = "",
        status = StrictRateCardThresholdStatus.REQUESTED,
    )
}
