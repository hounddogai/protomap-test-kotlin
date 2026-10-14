package com.acme.transport.fleetops.utilization.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization rollup is in its life cycle. */
enum class UtilizationRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One utilization rollup, with what the planners need to act on it. */
data class UtilizationRollup(
    var code: String,
    var name: String,
    var category: String,
    var capacity: Int,
    var description: String,
    var weight: Double,
    var pallets: Int,
    var currency: String,
    var minimum: Double,
    var reviewer: String,
    var depot: String,
    var status: UtilizationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationRollupStatus): Boolean {
        when (status) {
            UtilizationRollupStatus.OPEN -> return next == UtilizationRollupStatus.RESOLVED || next == UtilizationRollupStatus.REOPENED
            UtilizationRollupStatus.IN_REVIEW -> {
                when (next) {
                    UtilizationRollupStatus.RESOLVED, UtilizationRollupStatus.REOPENED -> return true
                }
                return false
            }
            UtilizationRollupStatus.RESOLVED -> {
                when (next) {
                    UtilizationRollupStatus.REOPENED -> return true
                }
                return false
            }
            UtilizationRollupStatus.REOPENED -> return next == UtilizationRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: UtilizationRollupStatus) {
        if (!canMoveTo(next)) {
            error("utilization rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((weight + pallets.toDouble()) * 100) / 100

    fun snapshot(): UtilizationRollup {
        return UtilizationRollup(
            code = code,
            name = name,
            category = category,
            capacity = capacity,
            description = description,
            weight = weight,
            pallets = pallets,
            currency = currency,
            minimum = minimum,
            reviewer = reviewer,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a utilization rollup in status open. */
fun newUtilizationRollup(code: String, name: String): UtilizationRollup {
    return UtilizationRollup(
        code = code,
        name = name,
        category = "",
        capacity = 0,
        description = "",
        weight = 0.0,
        pallets = 0,
        currency = "",
        minimum = 0.0,
        reviewer = "",
        depot = "",
        status = UtilizationRollupStatus.OPEN,
    )
}
