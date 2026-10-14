package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending violation snapshot is in its life cycle. */
enum class PendingViolationSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending violation snapshot as the violation team keeps it. */
data class PendingViolationSnapshot(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var label: String,
    var archived: Boolean,
    var channel: String,
    var fuelLiters: Double,
    var odometer: Int,
    var endsAt: Int,
    var revision: Int,
    var status: PendingViolationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingViolationSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingViolationSnapshotStatus): Boolean {
        when (status) {
            PendingViolationSnapshotStatus.DRAFT -> return next == PendingViolationSnapshotStatus.ACTIVE || next == PendingViolationSnapshotStatus.SUSPENDED
            PendingViolationSnapshotStatus.PENDING -> {
                when (next) {
                    PendingViolationSnapshotStatus.ACTIVE, PendingViolationSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingViolationSnapshotStatus.ACTIVE -> return next == PendingViolationSnapshotStatus.SUSPENDED
            PendingViolationSnapshotStatus.SUSPENDED -> return next == PendingViolationSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingViolationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending violation snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((fuelLiters + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingViolationSnapshot {
        return PendingViolationSnapshot(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            label = label,
            archived = archived,
            channel = channel,
            fuelLiters = fuelLiters,
            odometer = odometer,
            endsAt = endsAt,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending violation snapshot in status draft. */
fun newPendingViolationSnapshot(code: String, name: String): PendingViolationSnapshot {
    return PendingViolationSnapshot(
        code = code,
        name = name,
        etaMinutes = 0,
        label = "",
        archived = false,
        channel = "",
        fuelLiters = 0.0,
        odometer = 0,
        endsAt = 0,
        revision = 0,
        status = PendingViolationSnapshotStatus.DRAFT,
    )
}
