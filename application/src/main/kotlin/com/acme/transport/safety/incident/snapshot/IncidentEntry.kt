package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.normalize

/** Where a incident entry is in its life cycle. */
enum class IncidentEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A incident entry as the incident team keeps it. */
data class IncidentEntry(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var dock: String,
    var name: String,
    var locale: String,
    var approved: Boolean,
    var carrier: String,
    var quantity: Int,
    var visible: Boolean,
    var status: IncidentEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentEntryStatus): Boolean {
        when (status) {
            IncidentEntryStatus.PROPOSED -> return next == IncidentEntryStatus.WITHDRAWN
            IncidentEntryStatus.APPROVED -> {
                when (next) {
                    IncidentEntryStatus.REJECTED -> return true
                }
                return false
            }
            IncidentEntryStatus.REJECTED -> return next == IncidentEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: IncidentEntryStatus) {
        if (!canMoveTo(next)) {
            error("incident entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident entry"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): IncidentEntry {
        return IncidentEntry(
            code = code,
            label = label,
            pinned = pinned,
            dock = dock,
            name = name,
            locale = locale,
            approved = approved,
            carrier = carrier,
            quantity = quantity,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a incident entry in status proposed. */
fun newIncidentEntry(code: String, label: String): IncidentEntry {
    return IncidentEntry(
        code = code,
        label = label,
        pinned = false,
        dock = "",
        name = "",
        locale = "",
        approved = false,
        carrier = "",
        quantity = 0,
        visible = false,
        status = IncidentEntryStatus.PROPOSED,
    )
}
