package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.text.normalize

// The manual hub entry records of network's hub work, and the rules, stores, and calculations around them.

/** Where a manual hub entry is in its life cycle. */
enum class ManualHubEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual hub entry, with what the planners need to act on it. */
data class ManualHubEntry(
    var code: String,
    var title: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var lane: String,
    var capacity: Int,
    var status: ManualHubEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHubEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHubEntryStatus): Boolean {
        when (status) {
            ManualHubEntryStatus.DRAFT -> return next == ManualHubEntryStatus.SUSPENDED || next == ManualHubEntryStatus.CLOSED
            ManualHubEntryStatus.PENDING -> return next == ManualHubEntryStatus.SUSPENDED || next == ManualHubEntryStatus.CLOSED
            ManualHubEntryStatus.ACTIVE -> return next == ManualHubEntryStatus.CLOSED
            ManualHubEntryStatus.SUSPENDED -> {
                when (next) {
                    ManualHubEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualHubEntryStatus) {
        if (!canMoveTo(next)) {
            error("manual hub entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual hub entry"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): ManualHubEntry {
        return ManualHubEntry(
            code = code,
            title = title,
            vehicleId = vehicleId,
            attributes = attributes,
            lane = lane,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual hub entry in status draft. */
fun newManualHubEntry(code: String, title: String): ManualHubEntry {
    return ManualHubEntry(
        code = code,
        title = title,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        lane = "",
        capacity = 0,
        status = ManualHubEntryStatus.DRAFT,
    )
}
