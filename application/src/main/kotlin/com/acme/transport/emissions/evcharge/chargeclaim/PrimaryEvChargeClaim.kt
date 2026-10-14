package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.text.normalize

// The primary ev charge claim records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a primary ev charge claim is in its life cycle. */
enum class PrimaryEvChargeClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a primary ev charge claim and its current status. */
data class PrimaryEvChargeClaim(
    var code: String,
    var label: String,
    var dock: String,
    var pinned: Boolean,
    var maximum: Double,
    var status: PrimaryEvChargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeClaimStatus): Boolean {
        when (status) {
            PrimaryEvChargeClaimStatus.IDLE -> return next == PrimaryEvChargeClaimStatus.WARMING
            PrimaryEvChargeClaimStatus.WARMING -> {
                when (next) {
                    PrimaryEvChargeClaimStatus.DRAINING, PrimaryEvChargeClaimStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryEvChargeClaimStatus.READY -> return next == PrimaryEvChargeClaimStatus.STOPPED
            PrimaryEvChargeClaimStatus.DRAINING -> return next == PrimaryEvChargeClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = maximum

    fun snapshot(): PrimaryEvChargeClaim {
        return PrimaryEvChargeClaim(
            code = code,
            label = label,
            dock = dock,
            pinned = pinned,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary ev charge claim in status idle. */
fun newPrimaryEvChargeClaim(code: String, label: String): PrimaryEvChargeClaim {
    return PrimaryEvChargeClaim(
        code = code,
        label = label,
        dock = "",
        pinned = false,
        maximum = 0.0,
        status = PrimaryEvChargeClaimStatus.IDLE,
    )
}
