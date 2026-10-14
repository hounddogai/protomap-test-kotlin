package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time budget records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time budget is in its life cycle. */
enum class TransitTimeBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A transit time budget as the transit time team keeps it. */
data class TransitTimeBudget(
    var code: String,
    var title: String,
    var dock: String,
    var timezone: String,
    var approved: Boolean,
    var graceDays: Int,
    var contactEmail: String,
    var startsAt: Int,
    var active: Boolean,
    var unit: String,
    var stopCount: Int,
    var status: TransitTimeBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeBudgetStatus): Boolean {
        when (status) {
            TransitTimeBudgetStatus.DRAFT -> {
                when (next) {
                    TransitTimeBudgetStatus.ACTIVE, TransitTimeBudgetStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimeBudgetStatus.PENDING -> {
                when (next) {
                    TransitTimeBudgetStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimeBudgetStatus.ACTIVE -> {
                when (next) {
                    TransitTimeBudgetStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimeBudgetStatus.SUSPENDED -> return next == TransitTimeBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TransitTimeBudgetStatus) {
        if (!canMoveTo(next)) {
            error("transit time budget $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round(stopCount.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): TransitTimeBudget {
        return TransitTimeBudget(
            code = code,
            title = title,
            dock = dock,
            timezone = timezone,
            approved = approved,
            graceDays = graceDays,
            contactEmail = contactEmail,
            startsAt = startsAt,
            active = active,
            unit = unit,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a transit time budget in status draft. */
fun newTransitTimeBudget(code: String, title: String): TransitTimeBudget {
    return TransitTimeBudget(
        code = code,
        title = title,
        dock = "",
        timezone = "",
        approved = false,
        graceDays = 0,
        contactEmail = "",
        startsAt = 0,
        active = false,
        unit = "",
        stopCount = 0,
        status = TransitTimeBudgetStatus.DRAFT,
    )
}
