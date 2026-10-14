package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance draft is in its life cycle. */
enum class ClearanceDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance draft as the clearance team keeps it. */
data class ClearanceDraft(
    var code: String,
    var title: String,
    var graceDays: Int,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var version: Int,
    var batchSize: Int,
    var status: ClearanceDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceDraftStatus): Boolean {
        when (status) {
            ClearanceDraftStatus.IDLE -> {
                when (next) {
                    ClearanceDraftStatus.WARMING, ClearanceDraftStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceDraftStatus.WARMING -> return next == ClearanceDraftStatus.READY
            ClearanceDraftStatus.READY -> return next == ClearanceDraftStatus.STOPPED
            ClearanceDraftStatus.DRAINING -> return next == ClearanceDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ClearanceDraftStatus) {
        if (!canMoveTo(next)) {
            error("clearance draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((fuelLiters - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ClearanceDraft {
        return ClearanceDraft(
            code = code,
            title = title,
            graceDays = graceDays,
            tags = tags,
            fuelLiters = fuelLiters,
            version = version,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance draft in status idle. */
fun newClearanceDraft(code: String, title: String): ClearanceDraft {
    return ClearanceDraft(
        code = code,
        title = title,
        graceDays = 0,
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        version = 0,
        batchSize = 0,
        status = ClearanceDraftStatus.IDLE,
    )
}
