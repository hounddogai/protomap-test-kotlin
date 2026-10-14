package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted service plan entry is in its life cycle. */
enum class WeightedServicePlanEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a weighted service plan entry and its current status. */
data class WeightedServicePlanEntry(
    var code: String,
    var name: String,
    var unit: String,
    var batchSize: Int,
    var reviewer: String,
    var cost: Double,
    var version: Int,
    var endsAt: Int,
    var quantity: Int,
    var description: String,
    var minimum: Double,
    var status: WeightedServicePlanEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServicePlanEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServicePlanEntryStatus): Boolean {
        when (status) {
            WeightedServicePlanEntryStatus.OPEN -> {
                when (next) {
                    WeightedServicePlanEntryStatus.IN_REVIEW -> return true
                }
                return false
            }
            WeightedServicePlanEntryStatus.IN_REVIEW -> {
                when (next) {
                    WeightedServicePlanEntryStatus.RESOLVED -> return true
                }
                return false
            }
            WeightedServicePlanEntryStatus.RESOLVED -> {
                when (next) {
                    WeightedServicePlanEntryStatus.REOPENED -> return true
                }
                return false
            }
            WeightedServicePlanEntryStatus.REOPENED -> return next == WeightedServicePlanEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedServicePlanEntryStatus) {
        if (!canMoveTo(next)) {
            error("weighted service plan entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(batchSize.toDouble() * cost * 100) / 100

    fun snapshot(): WeightedServicePlanEntry {
        return WeightedServicePlanEntry(
            code = code,
            name = name,
            unit = unit,
            batchSize = batchSize,
            reviewer = reviewer,
            cost = cost,
            version = version,
            endsAt = endsAt,
            quantity = quantity,
            description = description,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted service plan entry in status open. */
fun newWeightedServicePlanEntry(code: String, name: String): WeightedServicePlanEntry {
    return WeightedServicePlanEntry(
        code = code,
        name = name,
        unit = "",
        batchSize = 0,
        reviewer = "",
        cost = 0.0,
        version = 0,
        endsAt = 0,
        quantity = 0,
        description = "",
        minimum = 0.0,
        status = WeightedServicePlanEntryStatus.OPEN,
    )
}
