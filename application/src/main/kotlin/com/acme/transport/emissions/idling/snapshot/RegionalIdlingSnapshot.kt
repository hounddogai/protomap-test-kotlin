package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.text.normalize

// The regional idling snapshot records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a regional idling snapshot is in its life cycle. */
enum class RegionalIdlingSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional idling snapshot, with what the planners need to act on it. */
data class RegionalIdlingSnapshot(
    var code: String,
    var label: String,
    var contactEmail: String,
    var rate: Double,
    var category: String,
    var status: RegionalIdlingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIdlingSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIdlingSnapshotStatus): Boolean {
        when (status) {
            RegionalIdlingSnapshotStatus.DRAFT -> return next == RegionalIdlingSnapshotStatus.ACTIVE
            RegionalIdlingSnapshotStatus.PENDING -> return next == RegionalIdlingSnapshotStatus.ACTIVE || next == RegionalIdlingSnapshotStatus.SUSPENDED
            RegionalIdlingSnapshotStatus.ACTIVE -> {
                when (next) {
                    RegionalIdlingSnapshotStatus.SUSPENDED, RegionalIdlingSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            RegionalIdlingSnapshotStatus.SUSPENDED -> return next == RegionalIdlingSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalIdlingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional idling snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = rate

    fun snapshot(): RegionalIdlingSnapshot {
        return RegionalIdlingSnapshot(
            code = code,
            label = label,
            contactEmail = contactEmail,
            rate = rate,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional idling snapshot in status draft. */
fun newRegionalIdlingSnapshot(code: String, label: String): RegionalIdlingSnapshot {
    return RegionalIdlingSnapshot(
        code = code,
        label = label,
        contactEmail = "",
        rate = 0.0,
        category = "",
        status = RegionalIdlingSnapshotStatus.DRAFT,
    )
}
