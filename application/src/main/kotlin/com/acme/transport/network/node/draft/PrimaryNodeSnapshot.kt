package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize

/** Where a primary node snapshot is in its life cycle. */
enum class PrimaryNodeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One primary node snapshot, with what the planners need to act on it. */
data class PrimaryNodeSnapshot(
    var code: String,
    var title: String,
    var unit: String,
    var checksum: String,
    var summary: String,
    var etaMinutes: Int,
    var currency: String,
    var status: PrimaryNodeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNodeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNodeSnapshotStatus): Boolean {
        when (status) {
            PrimaryNodeSnapshotStatus.CREATED -> return next == PrimaryNodeSnapshotStatus.PUBLISHED || next == PrimaryNodeSnapshotStatus.RETIRED
            PrimaryNodeSnapshotStatus.VALIDATED -> return next == PrimaryNodeSnapshotStatus.PUBLISHED || next == PrimaryNodeSnapshotStatus.RETIRED
            PrimaryNodeSnapshotStatus.PUBLISHED -> {
                when (next) {
                    PrimaryNodeSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNodeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary node snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): PrimaryNodeSnapshot {
        return PrimaryNodeSnapshot(
            code = code,
            title = title,
            unit = unit,
            checksum = checksum,
            summary = summary,
            etaMinutes = etaMinutes,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary node snapshot in status created. */
fun newPrimaryNodeSnapshot(code: String, title: String): PrimaryNodeSnapshot {
    return PrimaryNodeSnapshot(
        code = code,
        title = title,
        unit = "",
        checksum = "",
        summary = "",
        etaMinutes = 0,
        currency = "",
        status = PrimaryNodeSnapshotStatus.CREATED,
    )
}
