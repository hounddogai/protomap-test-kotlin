package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional parcel entry is in its life cycle. */
enum class RegionalParcelEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional parcel entry as the parcel team keeps it. */
data class RegionalParcelEntry(
    var code: String,
    var label: String,
    var locale: String,
    var startsAt: Int,
    var margin: Double,
    var rate: Double,
    var contactEmail: String,
    var status: RegionalParcelEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelEntryStatus): Boolean {
        when (status) {
            RegionalParcelEntryStatus.PLANNED -> {
                when (next) {
                    RegionalParcelEntryStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalParcelEntryStatus.SCHEDULED -> return next == RegionalParcelEntryStatus.IN_PROGRESS || next == RegionalParcelEntryStatus.CANCELLED
            RegionalParcelEntryStatus.IN_PROGRESS -> return next == RegionalParcelEntryStatus.COMPLETED || next == RegionalParcelEntryStatus.CANCELLED
            RegionalParcelEntryStatus.COMPLETED -> return next == RegionalParcelEntryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalParcelEntryStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((margin + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalParcelEntry {
        return RegionalParcelEntry(
            code = code,
            label = label,
            locale = locale,
            startsAt = startsAt,
            margin = margin,
            rate = rate,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional parcel entry in status planned. */
fun newRegionalParcelEntry(code: String, label: String): RegionalParcelEntry {
    return RegionalParcelEntry(
        code = code,
        label = label,
        locale = "",
        startsAt = 0,
        margin = 0.0,
        rate = 0.0,
        contactEmail = "",
        status = RegionalParcelEntryStatus.PLANNED,
    )
}
