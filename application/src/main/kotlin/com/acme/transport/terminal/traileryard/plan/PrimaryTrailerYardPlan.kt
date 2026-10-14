package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary trailer yard plan records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a primary trailer yard plan is in its life cycle. */
enum class PrimaryTrailerYardPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary trailer yard plan. Its code is unique within a tenant. */
data class PrimaryTrailerYardPlan(
    var code: String,
    var title: String,
    var ownerId: String,
    var floorValue: Double,
    var archived: Boolean,
    var budget: Double,
    var startsAt: Int,
    var active: Boolean,
    var expiresAt: Int,
    var capacity: Int,
    var status: PrimaryTrailerYardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerYardPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerYardPlanStatus): Boolean {
        when (status) {
            PrimaryTrailerYardPlanStatus.REQUESTED -> return next == PrimaryTrailerYardPlanStatus.REVOKED || next == PrimaryTrailerYardPlanStatus.EXPIRED
            PrimaryTrailerYardPlanStatus.GRANTED -> {
                when (next) {
                    PrimaryTrailerYardPlanStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryTrailerYardPlanStatus.REVOKED -> {
                when (next) {
                    PrimaryTrailerYardPlanStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerYardPlanStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer yard plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round(expiresAt.toDouble() * budget * 100) / 100

    fun snapshot(): PrimaryTrailerYardPlan {
        return PrimaryTrailerYardPlan(
            code = code,
            title = title,
            ownerId = ownerId,
            floorValue = floorValue,
            archived = archived,
            budget = budget,
            startsAt = startsAt,
            active = active,
            expiresAt = expiresAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary trailer yard plan in status requested. */
fun newPrimaryTrailerYardPlan(code: String, title: String): PrimaryTrailerYardPlan {
    return PrimaryTrailerYardPlan(
        code = code,
        title = title,
        ownerId = "",
        floorValue = 0.0,
        archived = false,
        budget = 0.0,
        startsAt = 0,
        active = false,
        expiresAt = 0,
        capacity = 0,
        status = PrimaryTrailerYardPlanStatus.REQUESTED,
    )
}
