package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary transit time budget is in its life cycle. */
enum class PrimaryTransitTimeBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a primary transit time budget and its current status. */
data class PrimaryTransitTimeBudget(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var sequence: Int,
    var depot: String,
    var status: PrimaryTransitTimeBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeBudgetStatus): Boolean {
        when (status) {
            PrimaryTransitTimeBudgetStatus.CREATED -> {
                when (next) {
                    PrimaryTransitTimeBudgetStatus.PUBLISHED, PrimaryTransitTimeBudgetStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryTransitTimeBudgetStatus.VALIDATED -> return next == PrimaryTransitTimeBudgetStatus.PUBLISHED || next == PrimaryTransitTimeBudgetStatus.RETIRED
            PrimaryTransitTimeBudgetStatus.PUBLISHED -> {
                when (next) {
                    PrimaryTransitTimeBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeBudgetStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): PrimaryTransitTimeBudget {
        return PrimaryTransitTimeBudget(
            code = code,
            name = name,
            updatedAt = updatedAt,
            sequence = sequence,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary transit time budget in status created. */
fun newPrimaryTransitTimeBudget(code: String, name: String): PrimaryTransitTimeBudget {
    return PrimaryTransitTimeBudget(
        code = code,
        name = name,
        updatedAt = 0,
        sequence = 0,
        depot = "",
        status = PrimaryTransitTimeBudgetStatus.CREATED,
    )
}
