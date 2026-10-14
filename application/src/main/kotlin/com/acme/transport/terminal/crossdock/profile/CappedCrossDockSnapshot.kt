package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cross dock snapshot is in its life cycle. */
enum class CappedCrossDockSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped cross dock snapshot. Its code is unique within a tenant. */
data class CappedCrossDockSnapshot(
    var code: String,
    var name: String,
    var phone: String,
    var segment: String,
    var capacity: Int,
    var endsAt: Int,
    var trailerId: String,
    var label: String,
    var title: String,
    var source: String,
    var status: CappedCrossDockSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCrossDockSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCrossDockSnapshotStatus): Boolean {
        when (status) {
            CappedCrossDockSnapshotStatus.PROPOSED -> return next == CappedCrossDockSnapshotStatus.REJECTED
            CappedCrossDockSnapshotStatus.APPROVED -> return next == CappedCrossDockSnapshotStatus.REJECTED || next == CappedCrossDockSnapshotStatus.WITHDRAWN
            CappedCrossDockSnapshotStatus.REJECTED -> return next == CappedCrossDockSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedCrossDockSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("capped cross dock snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun netExposure(): Double = round((endsAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): CappedCrossDockSnapshot {
        return CappedCrossDockSnapshot(
            code = code,
            name = name,
            phone = phone,
            segment = segment,
            capacity = capacity,
            endsAt = endsAt,
            trailerId = trailerId,
            label = label,
            title = title,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped cross dock snapshot in status proposed. */
fun newCappedCrossDockSnapshot(code: String, name: String): CappedCrossDockSnapshot {
    return CappedCrossDockSnapshot(
        code = code,
        name = name,
        phone = "",
        segment = "",
        capacity = 0,
        endsAt = 0,
        trailerId = "",
        label = "",
        title = "",
        source = "",
        status = CappedCrossDockSnapshotStatus.PROPOSED,
    )
}
