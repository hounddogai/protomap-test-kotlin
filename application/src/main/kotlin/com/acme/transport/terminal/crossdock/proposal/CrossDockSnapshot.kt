package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock snapshot is in its life cycle. */
enum class CrossDockSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a cross dock snapshot and its current status. */
data class CrossDockSnapshot(
    var code: String,
    var title: String,
    var notes: String,
    var floorValue: Double,
    var category: String,
    var loadKg: Double,
    var status: CrossDockSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockSnapshotStatus): Boolean {
        when (status) {
            CrossDockSnapshotStatus.PROPOSED -> return next == CrossDockSnapshotStatus.APPROVED || next == CrossDockSnapshotStatus.REJECTED
            CrossDockSnapshotStatus.APPROVED -> return next == CrossDockSnapshotStatus.REJECTED || next == CrossDockSnapshotStatus.WITHDRAWN
            CrossDockSnapshotStatus.REJECTED -> return next == CrossDockSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CrossDockSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("cross dock snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cross dock snapshot"} $code"

    fun effectiveTotal(): Double = round((floorValue - loadKg) * 100) / 100

    fun snapshot(): CrossDockSnapshot {
        return CrossDockSnapshot(
            code = code,
            title = title,
            notes = notes,
            floorValue = floorValue,
            category = category,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a cross dock snapshot in status proposed. */
fun newCrossDockSnapshot(code: String, title: String): CrossDockSnapshot {
    return CrossDockSnapshot(
        code = code,
        title = title,
        notes = "",
        floorValue = 0.0,
        category = "",
        loadKg = 0.0,
        status = CrossDockSnapshotStatus.PROPOSED,
    )
}
