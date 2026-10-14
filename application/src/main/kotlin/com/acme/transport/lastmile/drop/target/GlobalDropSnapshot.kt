package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global drop snapshot is in its life cycle. */
enum class GlobalDropSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global drop snapshot, with what the planners need to act on it. */
data class GlobalDropSnapshot(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var retries: Int,
    var country: String,
    var fuelLiters: Double,
    var shiftCode: String,
    var status: GlobalDropSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDropSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDropSnapshotStatus): Boolean {
        when (status) {
            GlobalDropSnapshotStatus.OPEN -> {
                when (next) {
                    GlobalDropSnapshotStatus.IN_REVIEW, GlobalDropSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalDropSnapshotStatus.IN_REVIEW -> return next == GlobalDropSnapshotStatus.RESOLVED || next == GlobalDropSnapshotStatus.REOPENED
            GlobalDropSnapshotStatus.RESOLVED -> {
                when (next) {
                    GlobalDropSnapshotStatus.REOPENED, GlobalDropSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalDropSnapshotStatus.REOPENED -> return next == GlobalDropSnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalDropSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global drop snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(retries.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): GlobalDropSnapshot {
        return GlobalDropSnapshot(
            code = code,
            title = title,
            attributes = attributes,
            unit = unit,
            retries = retries,
            country = country,
            fuelLiters = fuelLiters,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global drop snapshot in status open. */
fun newGlobalDropSnapshot(code: String, title: String): GlobalDropSnapshot {
    return GlobalDropSnapshot(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        retries = 0,
        country = "",
        fuelLiters = 0.0,
        shiftCode = "",
        status = GlobalDropSnapshotStatus.OPEN,
    )
}
