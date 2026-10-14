package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff claim records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff claim is in its life cycle. */
enum class CutoffClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cutoff claim. Its code is unique within a tenant. */
data class CutoffClaim(
    var code: String,
    var label: String,
    var cost: Double,
    var floorValue: Double,
    var endsAt: Int,
    var category: String,
    var version: Int,
    var active: Boolean,
    var status: CutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffClaimStatus): Boolean {
        when (status) {
            CutoffClaimStatus.IDLE -> {
                when (next) {
                    CutoffClaimStatus.WARMING, CutoffClaimStatus.DRAINING -> return true
                }
                return false
            }
            CutoffClaimStatus.WARMING -> {
                when (next) {
                    CutoffClaimStatus.READY, CutoffClaimStatus.STOPPED -> return true
                }
                return false
            }
            CutoffClaimStatus.READY -> {
                when (next) {
                    CutoffClaimStatus.DRAINING, CutoffClaimStatus.STOPPED -> return true
                }
                return false
            }
            CutoffClaimStatus.DRAINING -> return next == CutoffClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("cutoff claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((endsAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): CutoffClaim {
        return CutoffClaim(
            code = code,
            label = label,
            cost = cost,
            floorValue = floorValue,
            endsAt = endsAt,
            category = category,
            version = version,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a cutoff claim in status idle. */
fun newCutoffClaim(code: String, label: String): CutoffClaim {
    return CutoffClaim(
        code = code,
        label = label,
        cost = 0.0,
        floorValue = 0.0,
        endsAt = 0,
        category = "",
        version = 0,
        active = false,
        status = CutoffClaimStatus.IDLE,
    )
}
