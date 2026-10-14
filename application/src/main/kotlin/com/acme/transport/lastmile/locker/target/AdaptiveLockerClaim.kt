package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive locker claim is in its life cycle. */
enum class AdaptiveLockerClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive locker claim. Its code is unique within a tenant. */
data class AdaptiveLockerClaim(
    var code: String,
    var label: String,
    var depot: String,
    var rate: Double,
    var lane: String,
    var region: String,
    var ratio: Double,
    var etaMinutes: Int,
    var score: Double,
    var status: AdaptiveLockerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLockerClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLockerClaimStatus): Boolean {
        when (status) {
            AdaptiveLockerClaimStatus.IDLE -> {
                when (next) {
                    AdaptiveLockerClaimStatus.WARMING, AdaptiveLockerClaimStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveLockerClaimStatus.WARMING -> {
                when (next) {
                    AdaptiveLockerClaimStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveLockerClaimStatus.READY -> {
                when (next) {
                    AdaptiveLockerClaimStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveLockerClaimStatus.DRAINING -> return next == AdaptiveLockerClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveLockerClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive locker claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((score - rate) * 100) / 100

    fun snapshot(): AdaptiveLockerClaim {
        return AdaptiveLockerClaim(
            code = code,
            label = label,
            depot = depot,
            rate = rate,
            lane = lane,
            region = region,
            ratio = ratio,
            etaMinutes = etaMinutes,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive locker claim in status idle. */
fun newAdaptiveLockerClaim(code: String, label: String): AdaptiveLockerClaim {
    return AdaptiveLockerClaim(
        code = code,
        label = label,
        depot = "",
        rate = 0.0,
        lane = "",
        region = "",
        ratio = 0.0,
        etaMinutes = 0,
        score = 0.0,
        status = AdaptiveLockerClaimStatus.IDLE,
    )
}
