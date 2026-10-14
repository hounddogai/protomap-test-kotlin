package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive fuel burn claim records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a adaptive fuel burn claim is in its life cycle. */
enum class AdaptiveFuelBurnClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One adaptive fuel burn claim, with what the planners need to act on it. */
data class AdaptiveFuelBurnClaim(
    var code: String,
    var label: String,
    var description: String,
    var name: String,
    var visible: Boolean,
    var externalRef: String,
    var createdAt: Int,
    var batchSize: Int,
    var carrier: String,
    var status: AdaptiveFuelBurnClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFuelBurnClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFuelBurnClaimStatus): Boolean {
        when (status) {
            AdaptiveFuelBurnClaimStatus.IDLE -> {
                when (next) {
                    AdaptiveFuelBurnClaimStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveFuelBurnClaimStatus.WARMING -> return next == AdaptiveFuelBurnClaimStatus.READY || next == AdaptiveFuelBurnClaimStatus.STOPPED
            AdaptiveFuelBurnClaimStatus.READY -> return next == AdaptiveFuelBurnClaimStatus.STOPPED
            AdaptiveFuelBurnClaimStatus.DRAINING -> return next == AdaptiveFuelBurnClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveFuelBurnClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive fuel burn claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round(batchSize.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveFuelBurnClaim {
        return AdaptiveFuelBurnClaim(
            code = code,
            label = label,
            description = description,
            name = name,
            visible = visible,
            externalRef = externalRef,
            createdAt = createdAt,
            batchSize = batchSize,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive fuel burn claim in status idle. */
fun newAdaptiveFuelBurnClaim(code: String, label: String): AdaptiveFuelBurnClaim {
    return AdaptiveFuelBurnClaim(
        code = code,
        label = label,
        description = "",
        name = "",
        visible = false,
        externalRef = "",
        createdAt = 0,
        batchSize = 0,
        carrier = "",
        status = AdaptiveFuelBurnClaimStatus.IDLE,
    )
}
