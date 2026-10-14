package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone draft records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone draft is in its life cycle. */
enum class ZoneDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a zone draft and its current status. */
data class ZoneDraft(
    var code: String,
    var label: String,
    var capacity: Int,
    var notes: String,
    var endsAt: Int,
    var graceDays: Int,
    var title: String,
    var status: ZoneDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneDraftStatus): Boolean {
        when (status) {
            ZoneDraftStatus.PLANNED -> return next == ZoneDraftStatus.SCHEDULED || next == ZoneDraftStatus.COMPLETED
            ZoneDraftStatus.SCHEDULED -> return next == ZoneDraftStatus.IN_PROGRESS || next == ZoneDraftStatus.CANCELLED
            ZoneDraftStatus.IN_PROGRESS -> return next == ZoneDraftStatus.CANCELLED
            ZoneDraftStatus.COMPLETED -> {
                when (next) {
                    ZoneDraftStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneDraftStatus) {
        if (!canMoveTo(next)) {
            error("zone draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((graceDays.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): ZoneDraft {
        return ZoneDraft(
            code = code,
            label = label,
            capacity = capacity,
            notes = notes,
            endsAt = endsAt,
            graceDays = graceDays,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a zone draft in status planned. */
fun newZoneDraft(code: String, label: String): ZoneDraft {
    return ZoneDraft(
        code = code,
        label = label,
        capacity = 0,
        notes = "",
        endsAt = 0,
        graceDays = 0,
        title = "",
        status = ZoneDraftStatus.PLANNED,
    )
}
