package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted cross dock claim records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a weighted cross dock claim is in its life cycle. */
enum class WeightedCrossDockClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted cross dock claim as the cross dock team keeps it. */
data class WeightedCrossDockClaim(
    var code: String,
    var title: String,
    var visible: Boolean,
    var threshold: Double,
    var ceiling: Double,
    var vehicleId: String,
    var channel: String,
    var expiresAt: Int,
    var active: Boolean,
    var distanceKm: Double,
    var status: WeightedCrossDockClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCrossDockClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCrossDockClaimStatus): Boolean {
        when (status) {
            WeightedCrossDockClaimStatus.CREATED -> {
                when (next) {
                    WeightedCrossDockClaimStatus.RETIRED -> return true
                }
                return false
            }
            WeightedCrossDockClaimStatus.VALIDATED -> {
                when (next) {
                    WeightedCrossDockClaimStatus.PUBLISHED, WeightedCrossDockClaimStatus.RETIRED -> return true
                }
                return false
            }
            WeightedCrossDockClaimStatus.PUBLISHED -> {
                when (next) {
                    WeightedCrossDockClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCrossDockClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted cross dock claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((ceiling + threshold) * 100) / 100

    fun snapshot(): WeightedCrossDockClaim {
        return WeightedCrossDockClaim(
            code = code,
            title = title,
            visible = visible,
            threshold = threshold,
            ceiling = ceiling,
            vehicleId = vehicleId,
            channel = channel,
            expiresAt = expiresAt,
            active = active,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted cross dock claim in status created. */
fun newWeightedCrossDockClaim(code: String, title: String): WeightedCrossDockClaim {
    return WeightedCrossDockClaim(
        code = code,
        title = title,
        visible = false,
        threshold = 0.0,
        ceiling = 0.0,
        vehicleId = "",
        channel = "",
        expiresAt = 0,
        active = false,
        distanceKm = 0.0,
        status = WeightedCrossDockClaimStatus.CREATED,
    )
}
