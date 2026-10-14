package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive roster claim is in its life cycle. */
enum class AdaptiveRosterClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive roster claim as the roster team keeps it. */
data class AdaptiveRosterClaim(
    var code: String,
    var title: String,
    var floorValue: Double,
    var trailerId: String,
    var driverId: String,
    var expiresAt: Int,
    var stopCount: Int,
    var retries: Int,
    var pallets: Int,
    var weight: Double,
    var quantity: Int,
    var status: AdaptiveRosterClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRosterClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRosterClaimStatus): Boolean {
        when (status) {
            AdaptiveRosterClaimStatus.NEW -> return next == AdaptiveRosterClaimStatus.RUNNING
            AdaptiveRosterClaimStatus.QUEUED -> return next == AdaptiveRosterClaimStatus.RUNNING || next == AdaptiveRosterClaimStatus.DONE
            AdaptiveRosterClaimStatus.RUNNING -> return next == AdaptiveRosterClaimStatus.DONE
            AdaptiveRosterClaimStatus.DONE -> {
                when (next) {
                    AdaptiveRosterClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRosterClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive roster claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive roster claim"} $code"

    fun projectedVolume(): Double = round((pallets.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRosterClaim {
        return AdaptiveRosterClaim(
            code = code,
            title = title,
            floorValue = floorValue,
            trailerId = trailerId,
            driverId = driverId,
            expiresAt = expiresAt,
            stopCount = stopCount,
            retries = retries,
            pallets = pallets,
            weight = weight,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive roster claim in status new. */
fun newAdaptiveRosterClaim(code: String, title: String): AdaptiveRosterClaim {
    return AdaptiveRosterClaim(
        code = code,
        title = title,
        floorValue = 0.0,
        trailerId = "",
        driverId = "",
        expiresAt = 0,
        stopCount = 0,
        retries = 0,
        pallets = 0,
        weight = 0.0,
        quantity = 0,
        status = AdaptiveRosterClaimStatus.NEW,
    )
}
