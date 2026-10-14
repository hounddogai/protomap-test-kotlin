package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.normalize

// The ev charge claim records of emissions's ev charge work, and the rules, stores, and calculations around
// them.

/** Where a ev charge claim is in its life cycle. */
enum class EvChargeClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a ev charge claim and its current status. */
data class EvChargeClaim(
    var code: String,
    var title: String,
    var retries: Int,
    var country: String,
    var channel: String,
    var segment: String,
    var status: EvChargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeClaimStatus): Boolean {
        when (status) {
            EvChargeClaimStatus.CREATED -> return next == EvChargeClaimStatus.VALIDATED || next == EvChargeClaimStatus.RETIRED
            EvChargeClaimStatus.VALIDATED -> {
                when (next) {
                    EvChargeClaimStatus.PUBLISHED, EvChargeClaimStatus.RETIRED -> return true
                }
                return false
            }
            EvChargeClaimStatus.PUBLISHED -> {
                when (next) {
                    EvChargeClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("ev charge claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge claim"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): EvChargeClaim {
        return EvChargeClaim(
            code = code,
            title = title,
            retries = retries,
            country = country,
            channel = channel,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge claim in status created. */
fun newEvChargeClaim(code: String, title: String): EvChargeClaim {
    return EvChargeClaim(
        code = code,
        title = title,
        retries = 0,
        country = "",
        channel = "",
        segment = "",
        status = EvChargeClaimStatus.CREATED,
    )
}
