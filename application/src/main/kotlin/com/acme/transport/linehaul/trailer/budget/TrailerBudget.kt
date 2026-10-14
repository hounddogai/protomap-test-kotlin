package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer budget records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer budget is in its life cycle. */
enum class TrailerBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One trailer budget, with what the planners need to act on it. */
data class TrailerBudget(
    var code: String,
    var label: String,
    var approved: Boolean,
    var tags: MutableList<String>,
    var windowDays: Int,
    var revision: Int,
    var pallets: Int,
    var contactEmail: String,
    var amount: Double,
    var checksum: String,
    var budget: Double,
    var status: TrailerBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerBudgetStatus): Boolean {
        when (status) {
            TrailerBudgetStatus.CREATED -> return next == TrailerBudgetStatus.RETIRED
            TrailerBudgetStatus.VALIDATED -> return next == TrailerBudgetStatus.PUBLISHED
            TrailerBudgetStatus.PUBLISHED -> return next == TrailerBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerBudgetStatus) {
        if (!canMoveTo(next)) {
            error("trailer budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(pallets.toDouble() * amount * 100) / 100

    fun snapshot(): TrailerBudget {
        return TrailerBudget(
            code = code,
            label = label,
            approved = approved,
            tags = tags,
            windowDays = windowDays,
            revision = revision,
            pallets = pallets,
            contactEmail = contactEmail,
            amount = amount,
            checksum = checksum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer budget in status created. */
fun newTrailerBudget(code: String, label: String): TrailerBudget {
    return TrailerBudget(
        code = code,
        label = label,
        approved = false,
        tags = mutableListOf<String>(),
        windowDays = 0,
        revision = 0,
        pallets = 0,
        contactEmail = "",
        amount = 0.0,
        checksum = "",
        budget = 0.0,
        status = TrailerBudgetStatus.CREATED,
    )
}
