package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional proof of delivery baseline records of lastmile's proof of delivery work, and the rules,
// stores, and calculations around them.

/** Where a regional proof of delivery baseline is in its life cycle. */
enum class RegionalProofOfDeliveryBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional proof of delivery baseline, with what the planners need to act on it. */
data class RegionalProofOfDeliveryBaseline(
    var code: String,
    var title: String,
    var quantity: Int,
    var name: String,
    var currency: String,
    var capacity: Int,
    var status: RegionalProofOfDeliveryBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalProofOfDeliveryBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalProofOfDeliveryBaselineStatus): Boolean {
        when (status) {
            RegionalProofOfDeliveryBaselineStatus.REQUESTED -> {
                when (next) {
                    RegionalProofOfDeliveryBaselineStatus.REVOKED, RegionalProofOfDeliveryBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalProofOfDeliveryBaselineStatus.GRANTED -> return next == RegionalProofOfDeliveryBaselineStatus.REVOKED || next == RegionalProofOfDeliveryBaselineStatus.EXPIRED
            RegionalProofOfDeliveryBaselineStatus.REVOKED -> return next == RegionalProofOfDeliveryBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalProofOfDeliveryBaselineStatus) {
        if (!canMoveTo(next)) {
            error("regional proof of delivery baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((capacity.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalProofOfDeliveryBaseline {
        return RegionalProofOfDeliveryBaseline(
            code = code,
            title = title,
            quantity = quantity,
            name = name,
            currency = currency,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional proof of delivery baseline in status requested. */
fun newRegionalProofOfDeliveryBaseline(code: String, title: String): RegionalProofOfDeliveryBaseline {
    return RegionalProofOfDeliveryBaseline(
        code = code,
        title = title,
        quantity = 0,
        name = "",
        currency = "",
        capacity = 0,
        status = RegionalProofOfDeliveryBaselineStatus.REQUESTED,
    )
}
