package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal capacity snapshot records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a seasonal capacity snapshot is in its life cycle. */
enum class SeasonalCapacitySnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal capacity snapshot as the capacity team keeps it. */
data class SeasonalCapacitySnapshot(
    var code: String,
    var name: String,
    var attempts: Int,
    var weight: Double,
    var depot: String,
    var checksum: String,
    var batchSize: Int,
    var contactEmail: String,
    var pinned: Boolean,
    var status: SeasonalCapacitySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCapacitySnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCapacitySnapshotStatus): Boolean {
        when (status) {
            SeasonalCapacitySnapshotStatus.DRAFT -> {
                when (next) {
                    SeasonalCapacitySnapshotStatus.SUSPENDED, SeasonalCapacitySnapshotStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCapacitySnapshotStatus.PENDING -> {
                when (next) {
                    SeasonalCapacitySnapshotStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCapacitySnapshotStatus.ACTIVE -> {
                when (next) {
                    SeasonalCapacitySnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalCapacitySnapshotStatus.SUSPENDED -> {
                when (next) {
                    SeasonalCapacitySnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCapacitySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal capacity snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round((batchSize.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCapacitySnapshot {
        return SeasonalCapacitySnapshot(
            code = code,
            name = name,
            attempts = attempts,
            weight = weight,
            depot = depot,
            checksum = checksum,
            batchSize = batchSize,
            contactEmail = contactEmail,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal capacity snapshot in status draft. */
fun newSeasonalCapacitySnapshot(code: String, name: String): SeasonalCapacitySnapshot {
    return SeasonalCapacitySnapshot(
        code = code,
        name = name,
        attempts = 0,
        weight = 0.0,
        depot = "",
        checksum = "",
        batchSize = 0,
        contactEmail = "",
        pinned = false,
        status = SeasonalCapacitySnapshotStatus.DRAFT,
    )
}
