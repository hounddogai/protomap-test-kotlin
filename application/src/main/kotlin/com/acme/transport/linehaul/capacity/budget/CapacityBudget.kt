package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity budget records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity budget is in its life cycle. */
enum class CapacityBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capacity budget as the capacity team keeps it. */
data class CapacityBudget(
    var code: String,
    var title: String,
    var approved: Boolean,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var amount: Double,
    var notes: String,
    var status: CapacityBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityBudgetStatus): Boolean {
        when (status) {
            CapacityBudgetStatus.OPEN -> return next == CapacityBudgetStatus.IN_REVIEW || next == CapacityBudgetStatus.REOPENED
            CapacityBudgetStatus.IN_REVIEW -> {
                when (next) {
                    CapacityBudgetStatus.RESOLVED -> return true
                }
                return false
            }
            CapacityBudgetStatus.RESOLVED -> return next == CapacityBudgetStatus.REOPENED || next == CapacityBudgetStatus.ARCHIVED
            CapacityBudgetStatus.REOPENED -> return next == CapacityBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CapacityBudgetStatus) {
        if (!canMoveTo(next)) {
            error("capacity budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((amount + distanceKm) * 100) / 100

    fun snapshot(): CapacityBudget {
        return CapacityBudget(
            code = code,
            title = title,
            approved = approved,
            pinned = pinned,
            tags = tags,
            distanceKm = distanceKm,
            amount = amount,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capacity budget in status open. */
fun newCapacityBudget(code: String, title: String): CapacityBudget {
    return CapacityBudget(
        code = code,
        title = title,
        approved = false,
        pinned = false,
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        amount = 0.0,
        notes = "",
        status = CapacityBudgetStatus.OPEN,
    )
}
