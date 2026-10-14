package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling rest break entry records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rolling rest break entry is in its life cycle. */
enum class RollingRestBreakEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling rest break entry as the rest break team keeps it. */
data class RollingRestBreakEntry(
    var code: String,
    var label: String,
    var graceDays: Int,
    var contactEmail: String,
    var depot: String,
    var distanceKm: Double,
    var revision: Int,
    var createdAt: Int,
    var stopCount: Int,
    var tags: MutableList<String>,
    var externalRef: String,
    var status: RollingRestBreakEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRestBreakEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRestBreakEntryStatus): Boolean {
        when (status) {
            RollingRestBreakEntryStatus.REQUESTED -> {
                when (next) {
                    RollingRestBreakEntryStatus.REVOKED -> return true
                }
                return false
            }
            RollingRestBreakEntryStatus.GRANTED -> return next == RollingRestBreakEntryStatus.REVOKED || next == RollingRestBreakEntryStatus.EXPIRED
            RollingRestBreakEntryStatus.REVOKED -> return next == RollingRestBreakEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingRestBreakEntryStatus) {
        if (!canMoveTo(next)) {
            error("rolling rest break entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((revision.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RollingRestBreakEntry {
        return RollingRestBreakEntry(
            code = code,
            label = label,
            graceDays = graceDays,
            contactEmail = contactEmail,
            depot = depot,
            distanceKm = distanceKm,
            revision = revision,
            createdAt = createdAt,
            stopCount = stopCount,
            tags = tags,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling rest break entry in status requested. */
fun newRollingRestBreakEntry(code: String, label: String): RollingRestBreakEntry {
    return RollingRestBreakEntry(
        code = code,
        label = label,
        graceDays = 0,
        contactEmail = "",
        depot = "",
        distanceKm = 0.0,
        revision = 0,
        createdAt = 0,
        stopCount = 0,
        tags = mutableListOf<String>(),
        externalRef = "",
        status = RollingRestBreakEntryStatus.REQUESTED,
    )
}
