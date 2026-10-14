package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped scorecard policy is in its life cycle. */
enum class CappedScorecardPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped scorecard policy and its current status. */
data class CappedScorecardPolicy(
    var code: String,
    var name: String,
    var version: Int,
    var cost: Double,
    var reviewer: String,
    var capacity: Int,
    var createdAt: Int,
    var distanceKm: Double,
    var channel: String,
    var sequence: Int,
    var vehicleId: String,
    var status: CappedScorecardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardPolicyStatus): Boolean {
        when (status) {
            CappedScorecardPolicyStatus.PLANNED -> return next == CappedScorecardPolicyStatus.COMPLETED
            CappedScorecardPolicyStatus.SCHEDULED -> {
                when (next) {
                    CappedScorecardPolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CappedScorecardPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    CappedScorecardPolicyStatus.COMPLETED, CappedScorecardPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            CappedScorecardPolicyStatus.COMPLETED -> {
                when (next) {
                    CappedScorecardPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedScorecardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((capacity.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): CappedScorecardPolicy {
        return CappedScorecardPolicy(
            code = code,
            name = name,
            version = version,
            cost = cost,
            reviewer = reviewer,
            capacity = capacity,
            createdAt = createdAt,
            distanceKm = distanceKm,
            channel = channel,
            sequence = sequence,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped scorecard policy in status planned. */
fun newCappedScorecardPolicy(code: String, name: String): CappedScorecardPolicy {
    return CappedScorecardPolicy(
        code = code,
        name = name,
        version = 0,
        cost = 0.0,
        reviewer = "",
        capacity = 0,
        createdAt = 0,
        distanceKm = 0.0,
        channel = "",
        sequence = 0,
        vehicleId = "",
        status = CappedScorecardPolicyStatus.PLANNED,
    )
}
