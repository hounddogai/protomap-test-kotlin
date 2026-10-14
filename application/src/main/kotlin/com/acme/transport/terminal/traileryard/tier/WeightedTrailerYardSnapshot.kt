package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted trailer yard snapshot is in its life cycle. */
enum class WeightedTrailerYardSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One weighted trailer yard snapshot, with what the planners need to act on it. */
data class WeightedTrailerYardSnapshot(
    var code: String,
    var title: String,
    var quantity: Int,
    var vehicleId: String,
    var batchSize: Int,
    var unit: String,
    var status: WeightedTrailerYardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerYardSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerYardSnapshotStatus): Boolean {
        when (status) {
            WeightedTrailerYardSnapshotStatus.OPEN -> return next == WeightedTrailerYardSnapshotStatus.IN_REVIEW || next == WeightedTrailerYardSnapshotStatus.ARCHIVED
            WeightedTrailerYardSnapshotStatus.IN_REVIEW -> return next == WeightedTrailerYardSnapshotStatus.REOPENED
            WeightedTrailerYardSnapshotStatus.RESOLVED -> {
                when (next) {
                    WeightedTrailerYardSnapshotStatus.REOPENED -> return true
                }
                return false
            }
            WeightedTrailerYardSnapshotStatus.REOPENED -> {
                when (next) {
                    WeightedTrailerYardSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTrailerYardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer yard snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted trailer yard snapshot"} $code"

    fun adjustedVolume(): Double = round((batchSize.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedTrailerYardSnapshot {
        return WeightedTrailerYardSnapshot(
            code = code,
            title = title,
            quantity = quantity,
            vehicleId = vehicleId,
            batchSize = batchSize,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted trailer yard snapshot in status open. */
fun newWeightedTrailerYardSnapshot(code: String, title: String): WeightedTrailerYardSnapshot {
    return WeightedTrailerYardSnapshot(
        code = code,
        title = title,
        quantity = 0,
        vehicleId = "",
        batchSize = 0,
        unit = "",
        status = WeightedTrailerYardSnapshotStatus.OPEN,
    )
}
