package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize

// The seasonal proof of delivery allocation records of lastmile's proof of delivery work, and the rules,
// stores, and calculations around them.

/** Where a seasonal proof of delivery allocation is in its life cycle. */
enum class SeasonalProofOfDeliveryAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal proof of delivery allocation as the proof of delivery team keeps it. */
data class SeasonalProofOfDeliveryAllocation(
    var code: String,
    var label: String,
    var carrier: String,
    var ratio: Double,
    var reviewer: String,
    var routeCode: String,
    var status: SeasonalProofOfDeliveryAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalProofOfDeliveryAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalProofOfDeliveryAllocationStatus): Boolean {
        when (status) {
            SeasonalProofOfDeliveryAllocationStatus.NEW -> return next == SeasonalProofOfDeliveryAllocationStatus.QUEUED || next == SeasonalProofOfDeliveryAllocationStatus.FAILED
            SeasonalProofOfDeliveryAllocationStatus.QUEUED -> {
                when (next) {
                    SeasonalProofOfDeliveryAllocationStatus.FAILED -> return true
                }
                return false
            }
            SeasonalProofOfDeliveryAllocationStatus.RUNNING -> {
                when (next) {
                    SeasonalProofOfDeliveryAllocationStatus.DONE -> return true
                }
                return false
            }
            SeasonalProofOfDeliveryAllocationStatus.DONE -> return next == SeasonalProofOfDeliveryAllocationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalProofOfDeliveryAllocationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal proof of delivery allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal proof of delivery allocation"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): SeasonalProofOfDeliveryAllocation {
        return SeasonalProofOfDeliveryAllocation(
            code = code,
            label = label,
            carrier = carrier,
            ratio = ratio,
            reviewer = reviewer,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal proof of delivery allocation in status new. */
fun newSeasonalProofOfDeliveryAllocation(code: String, label: String): SeasonalProofOfDeliveryAllocation {
    return SeasonalProofOfDeliveryAllocation(
        code = code,
        label = label,
        carrier = "",
        ratio = 0.0,
        reviewer = "",
        routeCode = "",
        status = SeasonalProofOfDeliveryAllocationStatus.NEW,
    )
}
