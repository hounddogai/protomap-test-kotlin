package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance claim is in its life cycle. */
enum class ClearanceClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A clearance claim as the clearance team keeps it. */
data class ClearanceClaim(
    var code: String,
    var label: String,
    var notes: String,
    var cost: Double,
    var capacity: Int,
    var rate: Double,
    var startsAt: Int,
    var status: ClearanceClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceClaimStatus): Boolean {
        when (status) {
            ClearanceClaimStatus.CREATED -> {
                when (next) {
                    ClearanceClaimStatus.PUBLISHED, ClearanceClaimStatus.RETIRED -> return true
                }
                return false
            }
            ClearanceClaimStatus.VALIDATED -> return next == ClearanceClaimStatus.PUBLISHED || next == ClearanceClaimStatus.RETIRED
            ClearanceClaimStatus.PUBLISHED -> {
                when (next) {
                    ClearanceClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceClaimStatus) {
        if (!canMoveTo(next)) {
            error("clearance claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((rate + startsAt.toDouble()) * 100) / 100

    fun snapshot(): ClearanceClaim {
        return ClearanceClaim(
            code = code,
            label = label,
            notes = notes,
            cost = cost,
            capacity = capacity,
            rate = rate,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance claim in status created. */
fun newClearanceClaim(code: String, label: String): ClearanceClaim {
    return ClearanceClaim(
        code = code,
        label = label,
        notes = "",
        cost = 0.0,
        capacity = 0,
        rate = 0.0,
        startsAt = 0,
        status = ClearanceClaimStatus.CREATED,
    )
}
