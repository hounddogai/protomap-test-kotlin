package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard snapshot is in its life cycle. */
enum class TrailerYardSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One trailer yard snapshot, with what the planners need to act on it. */
data class TrailerYardSnapshot(
    var code: String,
    var name: String,
    var cost: Double,
    var ownerId: String,
    var batchSize: Int,
    var rate: Double,
    var category: String,
    var stopCount: Int,
    var ceiling: Double,
    var status: TrailerYardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardSnapshotStatus): Boolean {
        when (status) {
            TrailerYardSnapshotStatus.DRAFT -> return next == TrailerYardSnapshotStatus.SUSPENDED || next == TrailerYardSnapshotStatus.CLOSED
            TrailerYardSnapshotStatus.PENDING -> {
                when (next) {
                    TrailerYardSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardSnapshotStatus.ACTIVE -> {
                when (next) {
                    TrailerYardSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardSnapshotStatus.SUSPENDED -> return next == TrailerYardSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerYardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard snapshot"} $code"

    fun netValue(): Double = round((rate - batchSize.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardSnapshot {
        return TrailerYardSnapshot(
            code = code,
            name = name,
            cost = cost,
            ownerId = ownerId,
            batchSize = batchSize,
            rate = rate,
            category = category,
            stopCount = stopCount,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer yard snapshot in status draft. */
fun newTrailerYardSnapshot(code: String, name: String): TrailerYardSnapshot {
    return TrailerYardSnapshot(
        code = code,
        name = name,
        cost = 0.0,
        ownerId = "",
        batchSize = 0,
        rate = 0.0,
        category = "",
        stopCount = 0,
        ceiling = 0.0,
        status = TrailerYardSnapshotStatus.DRAFT,
    )
}
