package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn summary is in its life cycle. */
enum class FuelBurnSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel burn summary and its current status. */
data class FuelBurnSummary(
    var code: String,
    var label: String,
    var limit: Int,
    var quantity: Int,
    var description: String,
    var tags: MutableList<String>,
    var region: String,
    var notes: String,
    var ownerId: String,
    var status: FuelBurnSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnSummaryStatus): Boolean {
        when (status) {
            FuelBurnSummaryStatus.PLANNED -> {
                when (next) {
                    FuelBurnSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            FuelBurnSummaryStatus.SCHEDULED -> {
                when (next) {
                    FuelBurnSummaryStatus.IN_PROGRESS, FuelBurnSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            FuelBurnSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    FuelBurnSummaryStatus.COMPLETED, FuelBurnSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            FuelBurnSummaryStatus.COMPLETED -> return next == FuelBurnSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FuelBurnSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn summary $code cannot move from $status to $next")
        }
        status = next
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

    fun projectedTotal(): Double = round(quantity.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): FuelBurnSummary {
        return FuelBurnSummary(
            code = code,
            label = label,
            limit = limit,
            quantity = quantity,
            description = description,
            tags = tags,
            region = region,
            notes = notes,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel burn summary in status planned. */
fun newFuelBurnSummary(code: String, label: String): FuelBurnSummary {
    return FuelBurnSummary(
        code = code,
        label = label,
        limit = 0,
        quantity = 0,
        description = "",
        tags = mutableListOf<String>(),
        region = "",
        notes = "",
        ownerId = "",
        status = FuelBurnSummaryStatus.PLANNED,
    )
}
