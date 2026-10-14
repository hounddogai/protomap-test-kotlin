package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event snapshot is in its life cycle. */
enum class CameraEventSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a camera event snapshot and its current status. */
data class CameraEventSnapshot(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var attempts: Int,
    var vehicleId: String,
    var depot: String,
    var retries: Int,
    var budget: Double,
    var sequence: Int,
    var status: CameraEventSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventSnapshotStatus): Boolean {
        when (status) {
            CameraEventSnapshotStatus.DRAFT -> return next == CameraEventSnapshotStatus.PENDING || next == CameraEventSnapshotStatus.SUSPENDED
            CameraEventSnapshotStatus.PENDING -> return next == CameraEventSnapshotStatus.SUSPENDED
            CameraEventSnapshotStatus.ACTIVE -> return next == CameraEventSnapshotStatus.SUSPENDED || next == CameraEventSnapshotStatus.CLOSED
            CameraEventSnapshotStatus.SUSPENDED -> return next == CameraEventSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CameraEventSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("camera event snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((budget + sequence.toDouble()) * 100) / 100

    fun snapshot(): CameraEventSnapshot {
        return CameraEventSnapshot(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            attempts = attempts,
            vehicleId = vehicleId,
            depot = depot,
            retries = retries,
            budget = budget,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a camera event snapshot in status draft. */
fun newCameraEventSnapshot(code: String, title: String): CameraEventSnapshot {
    return CameraEventSnapshot(
        code = code,
        title = title,
        durationMinutes = 0,
        attempts = 0,
        vehicleId = "",
        depot = "",
        retries = 0,
        budget = 0.0,
        sequence = 0,
        status = CameraEventSnapshotStatus.DRAFT,
    )
}
