package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.normalize

/** Where a legacy incident entry is in its life cycle. */
enum class LegacyIncidentEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy incident entry. Its code is unique within a tenant. */
data class LegacyIncidentEntry(
    var code: String,
    var label: String,
    var country: String,
    var archived: Boolean,
    var timezone: String,
    var quantity: Int,
    var status: LegacyIncidentEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentEntryStatus): Boolean {
        when (status) {
            LegacyIncidentEntryStatus.PLANNED -> return next == LegacyIncidentEntryStatus.IN_PROGRESS || next == LegacyIncidentEntryStatus.CANCELLED
            LegacyIncidentEntryStatus.SCHEDULED -> {
                when (next) {
                    LegacyIncidentEntryStatus.IN_PROGRESS, LegacyIncidentEntryStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyIncidentEntryStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyIncidentEntryStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyIncidentEntryStatus.COMPLETED -> return next == LegacyIncidentEntryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyIncidentEntryStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LegacyIncidentEntry {
        return LegacyIncidentEntry(
            code = code,
            label = label,
            country = country,
            archived = archived,
            timezone = timezone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy incident entry in status planned. */
fun newLegacyIncidentEntry(code: String, label: String): LegacyIncidentEntry {
    return LegacyIncidentEntry(
        code = code,
        label = label,
        country = "",
        archived = false,
        timezone = "",
        quantity = 0,
        status = LegacyIncidentEntryStatus.PLANNED,
    )
}
