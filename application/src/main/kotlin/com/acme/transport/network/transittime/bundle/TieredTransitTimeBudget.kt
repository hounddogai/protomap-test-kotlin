package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered transit time budget is in its life cycle. */
enum class TieredTransitTimeBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tiered transit time budget and its current status. */
data class TieredTransitTimeBudget(
    var code: String,
    var name: String,
    var version: Int,
    var pallets: Int,
    var approved: Boolean,
    var odometer: Int,
    var region: String,
    var sequence: Int,
    var timezone: String,
    var vehicleId: String,
    var trailerId: String,
    var status: TieredTransitTimeBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeBudgetStatus): Boolean {
        when (status) {
            TieredTransitTimeBudgetStatus.DRAFT -> {
                when (next) {
                    TieredTransitTimeBudgetStatus.PENDING, TieredTransitTimeBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredTransitTimeBudgetStatus.PENDING -> return next == TieredTransitTimeBudgetStatus.SUSPENDED || next == TieredTransitTimeBudgetStatus.CLOSED
            TieredTransitTimeBudgetStatus.ACTIVE -> return next == TieredTransitTimeBudgetStatus.CLOSED
            TieredTransitTimeBudgetStatus.SUSPENDED -> {
                when (next) {
                    TieredTransitTimeBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time budget $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"tiered transit time budget"} $code"

    fun netValue(): Double = round((pallets.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): TieredTransitTimeBudget {
        return TieredTransitTimeBudget(
            code = code,
            name = name,
            version = version,
            pallets = pallets,
            approved = approved,
            odometer = odometer,
            region = region,
            sequence = sequence,
            timezone = timezone,
            vehicleId = vehicleId,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered transit time budget in status draft. */
fun newTieredTransitTimeBudget(code: String, name: String): TieredTransitTimeBudget {
    return TieredTransitTimeBudget(
        code = code,
        name = name,
        version = 0,
        pallets = 0,
        approved = false,
        odometer = 0,
        region = "",
        sequence = 0,
        timezone = "",
        vehicleId = "",
        trailerId = "",
        status = TieredTransitTimeBudgetStatus.DRAFT,
    )
}
