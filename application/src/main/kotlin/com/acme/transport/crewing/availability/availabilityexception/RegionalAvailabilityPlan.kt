package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional availability plan is in its life cycle. */
enum class RegionalAvailabilityPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional availability plan and its current status. */
data class RegionalAvailabilityPlan(
    var code: String,
    var name: String,
    var attempts: Int,
    var reviewer: String,
    var priority: Int,
    var contactEmail: String,
    var timezone: String,
    var routeCode: String,
    var status: RegionalAvailabilityPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAvailabilityPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAvailabilityPlanStatus): Boolean {
        when (status) {
            RegionalAvailabilityPlanStatus.CREATED -> {
                when (next) {
                    RegionalAvailabilityPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalAvailabilityPlanStatus.VALIDATED -> {
                when (next) {
                    RegionalAvailabilityPlanStatus.PUBLISHED, RegionalAvailabilityPlanStatus.RETIRED -> return true
                }
                return false
            }
            RegionalAvailabilityPlanStatus.PUBLISHED -> return next == RegionalAvailabilityPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalAvailabilityPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional availability plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round((attempts.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalAvailabilityPlan {
        return RegionalAvailabilityPlan(
            code = code,
            name = name,
            attempts = attempts,
            reviewer = reviewer,
            priority = priority,
            contactEmail = contactEmail,
            timezone = timezone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional availability plan in status created. */
fun newRegionalAvailabilityPlan(code: String, name: String): RegionalAvailabilityPlan {
    return RegionalAvailabilityPlan(
        code = code,
        name = name,
        attempts = 0,
        reviewer = "",
        priority = 0,
        contactEmail = "",
        timezone = "",
        routeCode = "",
        status = RegionalAvailabilityPlanStatus.CREATED,
    )
}
