package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending roster plan records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a pending roster plan is in its life cycle. */
enum class PendingRosterPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending roster plan, with what the planners need to act on it. */
data class PendingRosterPlan(
    var code: String,
    var name: String,
    var maximum: Double,
    var country: String,
    var checksum: String,
    var fuelLiters: Double,
    var status: PendingRosterPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRosterPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRosterPlanStatus): Boolean {
        when (status) {
            PendingRosterPlanStatus.CREATED -> return next == PendingRosterPlanStatus.RETIRED
            PendingRosterPlanStatus.VALIDATED -> return next == PendingRosterPlanStatus.RETIRED
            PendingRosterPlanStatus.PUBLISHED -> return next == PendingRosterPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingRosterPlanStatus) {
        if (!canMoveTo(next)) {
            error("pending roster plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((maximum + fuelLiters) * 100) / 100

    fun snapshot(): PendingRosterPlan {
        return PendingRosterPlan(
            code = code,
            name = name,
            maximum = maximum,
            country = country,
            checksum = checksum,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending roster plan in status created. */
fun newPendingRosterPlan(code: String, name: String): PendingRosterPlan {
    return PendingRosterPlan(
        code = code,
        name = name,
        maximum = 0.0,
        country = "",
        checksum = "",
        fuelLiters = 0.0,
        status = PendingRosterPlanStatus.CREATED,
    )
}
