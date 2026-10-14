package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tender allocation is in its life cycle. */
enum class WeightedTenderAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted tender allocation as the tender team keeps it. */
data class WeightedTenderAllocation(
    var code: String,
    var label: String,
    var sequence: Int,
    var endsAt: Int,
    var attempts: Int,
    var score: Double,
    var status: WeightedTenderAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTenderAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTenderAllocationStatus): Boolean {
        when (status) {
            WeightedTenderAllocationStatus.REQUESTED -> {
                when (next) {
                    WeightedTenderAllocationStatus.GRANTED -> return true
                }
                return false
            }
            WeightedTenderAllocationStatus.GRANTED -> return next == WeightedTenderAllocationStatus.REVOKED || next == WeightedTenderAllocationStatus.EXPIRED
            WeightedTenderAllocationStatus.REVOKED -> return next == WeightedTenderAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedTenderAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted tender allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round(endsAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): WeightedTenderAllocation {
        return WeightedTenderAllocation(
            code = code,
            label = label,
            sequence = sequence,
            endsAt = endsAt,
            attempts = attempts,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted tender allocation in status requested. */
fun newWeightedTenderAllocation(code: String, label: String): WeightedTenderAllocation {
    return WeightedTenderAllocation(
        code = code,
        label = label,
        sequence = 0,
        endsAt = 0,
        attempts = 0,
        score = 0.0,
        status = WeightedTenderAllocationStatus.REQUESTED,
    )
}
