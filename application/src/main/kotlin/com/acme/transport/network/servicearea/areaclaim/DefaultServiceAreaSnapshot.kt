package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default service area snapshot is in its life cycle. */
enum class DefaultServiceAreaSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default service area snapshot, with what the planners need to act on it. */
data class DefaultServiceAreaSnapshot(
    var code: String,
    var name: String,
    var sequence: Int,
    var floorValue: Double,
    var distanceKm: Double,
    var status: DefaultServiceAreaSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServiceAreaSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServiceAreaSnapshotStatus): Boolean {
        when (status) {
            DefaultServiceAreaSnapshotStatus.DRAFT -> return next == DefaultServiceAreaSnapshotStatus.ACTIVE || next == DefaultServiceAreaSnapshotStatus.CLOSED
            DefaultServiceAreaSnapshotStatus.PENDING -> return next == DefaultServiceAreaSnapshotStatus.CLOSED
            DefaultServiceAreaSnapshotStatus.ACTIVE -> return next == DefaultServiceAreaSnapshotStatus.CLOSED
            DefaultServiceAreaSnapshotStatus.SUSPENDED -> return next == DefaultServiceAreaSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultServiceAreaSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default service area snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default service area snapshot"} $code"

    fun netTotal(): Double = round((floorValue + distanceKm) * 100) / 100

    fun snapshot(): DefaultServiceAreaSnapshot {
        return DefaultServiceAreaSnapshot(
            code = code,
            name = name,
            sequence = sequence,
            floorValue = floorValue,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default service area snapshot in status draft. */
fun newDefaultServiceAreaSnapshot(code: String, name: String): DefaultServiceAreaSnapshot {
    return DefaultServiceAreaSnapshot(
        code = code,
        name = name,
        sequence = 0,
        floorValue = 0.0,
        distanceKm = 0.0,
        status = DefaultServiceAreaSnapshotStatus.DRAFT,
    )
}
