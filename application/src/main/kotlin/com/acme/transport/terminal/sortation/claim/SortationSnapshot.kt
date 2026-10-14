package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation snapshot is in its life cycle. */
enum class SortationSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One sortation snapshot, with what the planners need to act on it. */
data class SortationSnapshot(
    var code: String,
    var name: String,
    var sequence: Int,
    var lane: String,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var updatedAt: Int,
    var retries: Int,
    var ownerId: String,
    var shiftCode: String,
    var status: SortationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationSnapshotStatus): Boolean {
        when (status) {
            SortationSnapshotStatus.REQUESTED -> {
                when (next) {
                    SortationSnapshotStatus.REVOKED, SortationSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            SortationSnapshotStatus.GRANTED -> return next == SortationSnapshotStatus.REVOKED || next == SortationSnapshotStatus.EXPIRED
            SortationSnapshotStatus.REVOKED -> {
                when (next) {
                    SortationSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("sortation snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((version.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): SortationSnapshot {
        return SortationSnapshot(
            code = code,
            name = name,
            sequence = sequence,
            lane = lane,
            attributes = attributes,
            version = version,
            updatedAt = updatedAt,
            retries = retries,
            ownerId = ownerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a sortation snapshot in status requested. */
fun newSortationSnapshot(code: String, name: String): SortationSnapshot {
    return SortationSnapshot(
        code = code,
        name = name,
        sequence = 0,
        lane = "",
        attributes = mutableMapOf<String, String>(),
        version = 0,
        updatedAt = 0,
        retries = 0,
        ownerId = "",
        shiftCode = "",
        status = SortationSnapshotStatus.REQUESTED,
    )
}
