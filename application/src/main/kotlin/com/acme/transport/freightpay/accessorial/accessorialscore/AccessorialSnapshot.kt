package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial snapshot is in its life cycle. */
enum class AccessorialSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One accessorial snapshot, with what the planners need to act on it. */
data class AccessorialSnapshot(
    var code: String,
    var title: String,
    var visible: Boolean,
    var unit: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var status: AccessorialSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialSnapshotStatus): Boolean {
        when (status) {
            AccessorialSnapshotStatus.CREATED -> return next == AccessorialSnapshotStatus.RETIRED
            AccessorialSnapshotStatus.VALIDATED -> return next == AccessorialSnapshotStatus.PUBLISHED || next == AccessorialSnapshotStatus.RETIRED
            AccessorialSnapshotStatus.PUBLISHED -> {
                when (next) {
                    AccessorialSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("accessorial snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((durationMinutes.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): AccessorialSnapshot {
        return AccessorialSnapshot(
            code = code,
            title = title,
            visible = visible,
            unit = unit,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a accessorial snapshot in status created. */
fun newAccessorialSnapshot(code: String, title: String): AccessorialSnapshot {
    return AccessorialSnapshot(
        code = code,
        title = title,
        visible = false,
        unit = "",
        durationMinutes = 0,
        createdAt = 0,
        status = AccessorialSnapshotStatus.CREATED,
    )
}
