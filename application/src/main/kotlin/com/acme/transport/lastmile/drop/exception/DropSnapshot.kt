package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop snapshot is in its life cycle. */
enum class DropSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a drop snapshot and its current status. */
data class DropSnapshot(
    var code: String,
    var title: String,
    var driverId: String,
    var unit: String,
    var weight: Double,
    var notes: String,
    var sequence: Int,
    var archived: Boolean,
    var ownerId: String,
    var maximum: Double,
    var limit: Int,
    var status: DropSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropSnapshotStatus): Boolean {
        when (status) {
            DropSnapshotStatus.IDLE -> {
                when (next) {
                    DropSnapshotStatus.WARMING, DropSnapshotStatus.DRAINING -> return true
                }
                return false
            }
            DropSnapshotStatus.WARMING -> return next == DropSnapshotStatus.DRAINING || next == DropSnapshotStatus.STOPPED
            DropSnapshotStatus.READY -> {
                when (next) {
                    DropSnapshotStatus.DRAINING, DropSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            DropSnapshotStatus.DRAINING -> {
                when (next) {
                    DropSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("drop snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((maximum + limit.toDouble()) * 100) / 100

    fun snapshot(): DropSnapshot {
        return DropSnapshot(
            code = code,
            title = title,
            driverId = driverId,
            unit = unit,
            weight = weight,
            notes = notes,
            sequence = sequence,
            archived = archived,
            ownerId = ownerId,
            maximum = maximum,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a drop snapshot in status idle. */
fun newDropSnapshot(code: String, title: String): DropSnapshot {
    return DropSnapshot(
        code = code,
        title = title,
        driverId = "",
        unit = "",
        weight = 0.0,
        notes = "",
        sequence = 0,
        archived = false,
        ownerId = "",
        maximum = 0.0,
        limit = 0,
        status = DropSnapshotStatus.IDLE,
    )
}
