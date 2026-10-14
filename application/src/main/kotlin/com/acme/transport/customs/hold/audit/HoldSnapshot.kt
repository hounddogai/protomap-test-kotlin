package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.text.normalize

/** Where a hold snapshot is in its life cycle. */
enum class HoldSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One hold snapshot, with what the planners need to act on it. */
data class HoldSnapshot(
    var code: String,
    var label: String,
    var dock: String,
    var retries: Int,
    var phone: String,
    var active: Boolean,
    var status: HoldSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldSnapshotStatus): Boolean {
        when (status) {
            HoldSnapshotStatus.PLANNED -> {
                when (next) {
                    HoldSnapshotStatus.SCHEDULED, HoldSnapshotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            HoldSnapshotStatus.SCHEDULED -> {
                when (next) {
                    HoldSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            HoldSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    HoldSnapshotStatus.COMPLETED, HoldSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            HoldSnapshotStatus.COMPLETED -> return next == HoldSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HoldSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("hold snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): HoldSnapshot {
        return HoldSnapshot(
            code = code,
            label = label,
            dock = dock,
            retries = retries,
            phone = phone,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a hold snapshot in status planned. */
fun newHoldSnapshot(code: String, label: String): HoldSnapshot {
    return HoldSnapshot(
        code = code,
        label = label,
        dock = "",
        retries = 0,
        phone = "",
        active = false,
        status = HoldSnapshotStatus.PLANNED,
    )
}
