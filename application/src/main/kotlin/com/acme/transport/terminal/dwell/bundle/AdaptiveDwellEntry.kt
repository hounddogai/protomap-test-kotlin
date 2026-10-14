package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dwell entry is in its life cycle. */
enum class AdaptiveDwellEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive dwell entry and its current status. */
data class AdaptiveDwellEntry(
    var code: String,
    var name: String,
    var externalRef: String,
    var startsAt: Int,
    var pinned: Boolean,
    var updatedAt: Int,
    var reviewer: String,
    var capacity: Int,
    var stopCount: Int,
    var limit: Int,
    var etaMinutes: Int,
    var status: AdaptiveDwellEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDwellEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDwellEntryStatus): Boolean {
        when (status) {
            AdaptiveDwellEntryStatus.PLANNED -> return next == AdaptiveDwellEntryStatus.COMPLETED
            AdaptiveDwellEntryStatus.SCHEDULED -> return next == AdaptiveDwellEntryStatus.CANCELLED
            AdaptiveDwellEntryStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveDwellEntryStatus.COMPLETED, AdaptiveDwellEntryStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveDwellEntryStatus.COMPLETED -> {
                when (next) {
                    AdaptiveDwellEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDwellEntryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dwell entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round(capacity.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): AdaptiveDwellEntry {
        return AdaptiveDwellEntry(
            code = code,
            name = name,
            externalRef = externalRef,
            startsAt = startsAt,
            pinned = pinned,
            updatedAt = updatedAt,
            reviewer = reviewer,
            capacity = capacity,
            stopCount = stopCount,
            limit = limit,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive dwell entry in status planned. */
fun newAdaptiveDwellEntry(code: String, name: String): AdaptiveDwellEntry {
    return AdaptiveDwellEntry(
        code = code,
        name = name,
        externalRef = "",
        startsAt = 0,
        pinned = false,
        updatedAt = 0,
        reviewer = "",
        capacity = 0,
        stopCount = 0,
        limit = 0,
        etaMinutes = 0,
        status = AdaptiveDwellEntryStatus.PLANNED,
    )
}
