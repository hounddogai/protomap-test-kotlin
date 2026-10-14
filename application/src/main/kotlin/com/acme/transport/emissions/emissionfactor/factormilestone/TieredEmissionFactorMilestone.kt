package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered emission factor milestone records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a tiered emission factor milestone is in its life cycle. */
enum class TieredEmissionFactorMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered emission factor milestone. Its code is unique within a tenant. */
data class TieredEmissionFactorMilestone(
    var code: String,
    var label: String,
    var description: String,
    var capacity: Int,
    var approved: Boolean,
    var priority: Int,
    var source: String,
    var routeCode: String,
    var trailerId: String,
    var status: TieredEmissionFactorMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEmissionFactorMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEmissionFactorMilestoneStatus): Boolean {
        when (status) {
            TieredEmissionFactorMilestoneStatus.PROPOSED -> return next == TieredEmissionFactorMilestoneStatus.REJECTED || next == TieredEmissionFactorMilestoneStatus.WITHDRAWN
            TieredEmissionFactorMilestoneStatus.APPROVED -> return next == TieredEmissionFactorMilestoneStatus.REJECTED || next == TieredEmissionFactorMilestoneStatus.WITHDRAWN
            TieredEmissionFactorMilestoneStatus.REJECTED -> return next == TieredEmissionFactorMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredEmissionFactorMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tiered emission factor milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((capacity.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): TieredEmissionFactorMilestone {
        return TieredEmissionFactorMilestone(
            code = code,
            label = label,
            description = description,
            capacity = capacity,
            approved = approved,
            priority = priority,
            source = source,
            routeCode = routeCode,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered emission factor milestone in status proposed. */
fun newTieredEmissionFactorMilestone(code: String, label: String): TieredEmissionFactorMilestone {
    return TieredEmissionFactorMilestone(
        code = code,
        label = label,
        description = "",
        capacity = 0,
        approved = false,
        priority = 0,
        source = "",
        routeCode = "",
        trailerId = "",
        status = TieredEmissionFactorMilestoneStatus.PROPOSED,
    )
}
