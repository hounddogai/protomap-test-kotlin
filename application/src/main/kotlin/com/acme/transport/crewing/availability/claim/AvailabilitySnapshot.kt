package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability snapshot is in its life cycle. */
enum class AvailabilitySnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A availability snapshot. Its code is unique within a tenant. */
data class AvailabilitySnapshot(
    var code: String,
    var name: String,
    var currency: String,
    var budget: Double,
    var sequence: Int,
    var dock: String,
    var status: AvailabilitySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilitySnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilitySnapshotStatus): Boolean {
        when (status) {
            AvailabilitySnapshotStatus.CREATED -> {
                when (next) {
                    AvailabilitySnapshotStatus.RETIRED -> return true
                }
                return false
            }
            AvailabilitySnapshotStatus.VALIDATED -> {
                when (next) {
                    AvailabilitySnapshotStatus.PUBLISHED, AvailabilitySnapshotStatus.RETIRED -> return true
                }
                return false
            }
            AvailabilitySnapshotStatus.PUBLISHED -> return next == AvailabilitySnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AvailabilitySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("availability snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((sequence.toDouble() + budget) * 100) / 100

    fun snapshot(): AvailabilitySnapshot {
        return AvailabilitySnapshot(
            code = code,
            name = name,
            currency = currency,
            budget = budget,
            sequence = sequence,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability snapshot in status created. */
fun newAvailabilitySnapshot(code: String, name: String): AvailabilitySnapshot {
    return AvailabilitySnapshot(
        code = code,
        name = name,
        currency = "",
        budget = 0.0,
        sequence = 0,
        dock = "",
        status = AvailabilitySnapshotStatus.CREATED,
    )
}
