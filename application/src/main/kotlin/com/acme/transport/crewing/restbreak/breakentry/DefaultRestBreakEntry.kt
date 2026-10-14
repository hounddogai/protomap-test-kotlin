package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default rest break entry records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a default rest break entry is in its life cycle. */
enum class DefaultRestBreakEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default rest break entry as the rest break team keeps it. */
data class DefaultRestBreakEntry(
    var code: String,
    var name: String,
    var cost: Double,
    var windowDays: Int,
    var region: String,
    var ownerId: String,
    var active: Boolean,
    var discount: Double,
    var notes: String,
    var status: DefaultRestBreakEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRestBreakEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRestBreakEntryStatus): Boolean {
        when (status) {
            DefaultRestBreakEntryStatus.PLANNED -> return next == DefaultRestBreakEntryStatus.SCHEDULED || next == DefaultRestBreakEntryStatus.COMPLETED
            DefaultRestBreakEntryStatus.SCHEDULED -> return next == DefaultRestBreakEntryStatus.COMPLETED || next == DefaultRestBreakEntryStatus.CANCELLED
            DefaultRestBreakEntryStatus.IN_PROGRESS -> return next == DefaultRestBreakEntryStatus.COMPLETED
            DefaultRestBreakEntryStatus.COMPLETED -> return next == DefaultRestBreakEntryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultRestBreakEntryStatus) {
        if (!canMoveTo(next)) {
            error("default rest break entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round(cost * discount * 100) / 100

    fun snapshot(): DefaultRestBreakEntry {
        return DefaultRestBreakEntry(
            code = code,
            name = name,
            cost = cost,
            windowDays = windowDays,
            region = region,
            ownerId = ownerId,
            active = active,
            discount = discount,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default rest break entry in status planned. */
fun newDefaultRestBreakEntry(code: String, name: String): DefaultRestBreakEntry {
    return DefaultRestBreakEntry(
        code = code,
        name = name,
        cost = 0.0,
        windowDays = 0,
        region = "",
        ownerId = "",
        active = false,
        discount = 0.0,
        notes = "",
        status = DefaultRestBreakEntryStatus.PLANNED,
    )
}
