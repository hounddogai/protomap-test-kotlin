package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability budget is in its life cycle. */
enum class AvailabilityBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A availability budget. Its code is unique within a tenant. */
data class AvailabilityBudget(
    var code: String,
    var name: String,
    var createdAt: Int,
    var reviewer: String,
    var margin: Double,
    var ratio: Double,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var status: AvailabilityBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityBudgetStatus): Boolean {
        when (status) {
            AvailabilityBudgetStatus.DRAFT -> {
                when (next) {
                    AvailabilityBudgetStatus.PENDING, AvailabilityBudgetStatus.CLOSED -> return true
                }
                return false
            }
            AvailabilityBudgetStatus.PENDING -> return next == AvailabilityBudgetStatus.ACTIVE
            AvailabilityBudgetStatus.ACTIVE -> return next == AvailabilityBudgetStatus.SUSPENDED
            AvailabilityBudgetStatus.SUSPENDED -> {
                when (next) {
                    AvailabilityBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityBudgetStatus) {
        if (!canMoveTo(next)) {
            error("availability budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"availability budget"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((cost + createdAt.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityBudget {
        return AvailabilityBudget(
            code = code,
            name = name,
            createdAt = createdAt,
            reviewer = reviewer,
            margin = margin,
            ratio = ratio,
            cost = cost,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability budget in status draft. */
fun newAvailabilityBudget(code: String, name: String): AvailabilityBudget {
    return AvailabilityBudget(
        code = code,
        name = name,
        createdAt = 0,
        reviewer = "",
        margin = 0.0,
        ratio = 0.0,
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = AvailabilityBudgetStatus.DRAFT,
    )
}
