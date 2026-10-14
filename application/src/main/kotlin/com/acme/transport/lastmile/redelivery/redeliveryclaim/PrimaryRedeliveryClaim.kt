package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary redelivery claim records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a primary redelivery claim is in its life cycle. */
enum class PrimaryRedeliveryClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary redelivery claim as the redelivery team keeps it. */
data class PrimaryRedeliveryClaim(
    var code: String,
    var label: String,
    var phone: String,
    var channel: String,
    var distanceKm: Double,
    var weight: Double,
    var status: PrimaryRedeliveryClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRedeliveryClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRedeliveryClaimStatus): Boolean {
        when (status) {
            PrimaryRedeliveryClaimStatus.DRAFT -> {
                when (next) {
                    PrimaryRedeliveryClaimStatus.SUSPENDED, PrimaryRedeliveryClaimStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryRedeliveryClaimStatus.PENDING -> {
                when (next) {
                    PrimaryRedeliveryClaimStatus.ACTIVE -> return true
                }
                return false
            }
            PrimaryRedeliveryClaimStatus.ACTIVE -> return next == PrimaryRedeliveryClaimStatus.SUSPENDED || next == PrimaryRedeliveryClaimStatus.CLOSED
            PrimaryRedeliveryClaimStatus.SUSPENDED -> return next == PrimaryRedeliveryClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryRedeliveryClaimStatus) {
        if (!canMoveTo(next)) {
            error("primary redelivery claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((distanceKm - weight) * 100) / 100

    fun snapshot(): PrimaryRedeliveryClaim {
        return PrimaryRedeliveryClaim(
            code = code,
            label = label,
            phone = phone,
            channel = channel,
            distanceKm = distanceKm,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary redelivery claim in status draft. */
fun newPrimaryRedeliveryClaim(code: String, label: String): PrimaryRedeliveryClaim {
    return PrimaryRedeliveryClaim(
        code = code,
        label = label,
        phone = "",
        channel = "",
        distanceKm = 0.0,
        weight = 0.0,
        status = PrimaryRedeliveryClaimStatus.DRAFT,
    )
}
