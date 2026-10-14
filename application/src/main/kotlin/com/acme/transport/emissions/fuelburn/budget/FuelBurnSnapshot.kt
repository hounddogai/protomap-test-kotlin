package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn snapshot is in its life cycle. */
enum class FuelBurnSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fuel burn snapshot and its current status. */
data class FuelBurnSnapshot(
    var code: String,
    var name: String,
    var minimum: Double,
    var weight: Double,
    var limit: Int,
    var odometer: Int,
    var graceDays: Int,
    var timezone: String,
    var channel: String,
    var externalRef: String,
    var status: FuelBurnSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnSnapshotStatus): Boolean {
        when (status) {
            FuelBurnSnapshotStatus.DRAFT -> {
                when (next) {
                    FuelBurnSnapshotStatus.SUSPENDED, FuelBurnSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            FuelBurnSnapshotStatus.PENDING -> {
                when (next) {
                    FuelBurnSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            FuelBurnSnapshotStatus.ACTIVE -> return next == FuelBurnSnapshotStatus.CLOSED
            FuelBurnSnapshotStatus.SUSPENDED -> return next == FuelBurnSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FuelBurnSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn snapshot"} $code"

    fun adjustedValue(): Double = round(graceDays.toDouble() * minimum * 100) / 100

    fun snapshot(): FuelBurnSnapshot {
        return FuelBurnSnapshot(
            code = code,
            name = name,
            minimum = minimum,
            weight = weight,
            limit = limit,
            odometer = odometer,
            graceDays = graceDays,
            timezone = timezone,
            channel = channel,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fuel burn snapshot in status draft. */
fun newFuelBurnSnapshot(code: String, name: String): FuelBurnSnapshot {
    return FuelBurnSnapshot(
        code = code,
        name = name,
        minimum = 0.0,
        weight = 0.0,
        limit = 0,
        odometer = 0,
        graceDays = 0,
        timezone = "",
        channel = "",
        externalRef = "",
        status = FuelBurnSnapshotStatus.DRAFT,
    )
}
