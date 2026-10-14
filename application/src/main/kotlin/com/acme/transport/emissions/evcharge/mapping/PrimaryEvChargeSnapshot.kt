package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.normalize

/** Where a primary ev charge snapshot is in its life cycle. */
enum class PrimaryEvChargeSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One primary ev charge snapshot, with what the planners need to act on it. */
data class PrimaryEvChargeSnapshot(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var visible: Boolean,
    var durationMinutes: Int,
    var dock: String,
    var status: PrimaryEvChargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeSnapshotStatus): Boolean {
        when (status) {
            PrimaryEvChargeSnapshotStatus.PLANNED -> {
                when (next) {
                    PrimaryEvChargeSnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryEvChargeSnapshotStatus.SCHEDULED -> return next == PrimaryEvChargeSnapshotStatus.IN_PROGRESS || next == PrimaryEvChargeSnapshotStatus.CANCELLED
            PrimaryEvChargeSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryEvChargeSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryEvChargeSnapshotStatus.COMPLETED -> return next == PrimaryEvChargeSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): PrimaryEvChargeSnapshot {
        return PrimaryEvChargeSnapshot(
            code = code,
            title = title,
            tags = tags,
            visible = visible,
            durationMinutes = durationMinutes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary ev charge snapshot in status planned. */
fun newPrimaryEvChargeSnapshot(code: String, title: String): PrimaryEvChargeSnapshot {
    return PrimaryEvChargeSnapshot(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        visible = false,
        durationMinutes = 0,
        dock = "",
        status = PrimaryEvChargeSnapshotStatus.PLANNED,
    )
}
