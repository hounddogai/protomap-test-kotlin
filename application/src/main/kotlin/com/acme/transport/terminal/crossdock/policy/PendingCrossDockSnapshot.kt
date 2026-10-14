package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cross dock snapshot is in its life cycle. */
enum class PendingCrossDockSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending cross dock snapshot. Its code is unique within a tenant. */
data class PendingCrossDockSnapshot(
    var code: String,
    var label: String,
    var startsAt: Int,
    var etaMinutes: Int,
    var quantity: Int,
    var approved: Boolean,
    var lane: String,
    var segment: String,
    var dock: String,
    var trailerId: String,
    var status: PendingCrossDockSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCrossDockSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCrossDockSnapshotStatus): Boolean {
        when (status) {
            PendingCrossDockSnapshotStatus.CREATED -> {
                when (next) {
                    PendingCrossDockSnapshotStatus.VALIDATED -> return true
                }
                return false
            }
            PendingCrossDockSnapshotStatus.VALIDATED -> return next == PendingCrossDockSnapshotStatus.RETIRED
            PendingCrossDockSnapshotStatus.PUBLISHED -> return next == PendingCrossDockSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingCrossDockSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending cross dock snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round(startsAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): PendingCrossDockSnapshot {
        return PendingCrossDockSnapshot(
            code = code,
            label = label,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            quantity = quantity,
            approved = approved,
            lane = lane,
            segment = segment,
            dock = dock,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending cross dock snapshot in status created. */
fun newPendingCrossDockSnapshot(code: String, label: String): PendingCrossDockSnapshot {
    return PendingCrossDockSnapshot(
        code = code,
        label = label,
        startsAt = 0,
        etaMinutes = 0,
        quantity = 0,
        approved = false,
        lane = "",
        segment = "",
        dock = "",
        trailerId = "",
        status = PendingCrossDockSnapshotStatus.CREATED,
    )
}
