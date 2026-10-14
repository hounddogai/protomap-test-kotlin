package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.normalize

// The scheduled trailer yard entry records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a scheduled trailer yard entry is in its life cycle. */
enum class ScheduledTrailerYardEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled trailer yard entry as the trailer yard team keeps it. */
data class ScheduledTrailerYardEntry(
    var code: String,
    var name: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var carrier: String,
    var status: ScheduledTrailerYardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerYardEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerYardEntryStatus): Boolean {
        when (status) {
            ScheduledTrailerYardEntryStatus.NEW -> return next == ScheduledTrailerYardEntryStatus.DONE || next == ScheduledTrailerYardEntryStatus.FAILED
            ScheduledTrailerYardEntryStatus.QUEUED -> return next == ScheduledTrailerYardEntryStatus.RUNNING
            ScheduledTrailerYardEntryStatus.RUNNING -> return next == ScheduledTrailerYardEntryStatus.DONE
            ScheduledTrailerYardEntryStatus.DONE -> {
                when (next) {
                    ScheduledTrailerYardEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerYardEntryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer yard entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): ScheduledTrailerYardEntry {
        return ScheduledTrailerYardEntry(
            code = code,
            name = name,
            vehicleId = vehicleId,
            attributes = attributes,
            version = version,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled trailer yard entry in status new. */
fun newScheduledTrailerYardEntry(code: String, name: String): ScheduledTrailerYardEntry {
    return ScheduledTrailerYardEntry(
        code = code,
        name = name,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        version = 0,
        carrier = "",
        status = ScheduledTrailerYardEntryStatus.NEW,
    )
}
