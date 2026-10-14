package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel entry is in its life cycle. */
enum class ParcelEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A parcel entry. Its code is unique within a tenant. */
data class ParcelEntry(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var description: String,
    var graceDays: Int,
    var status: ParcelEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelEntryStatus): Boolean {
        when (status) {
            ParcelEntryStatus.DRAFT -> {
                when (next) {
                    ParcelEntryStatus.CLOSED -> return true
                }
                return false
            }
            ParcelEntryStatus.PENDING -> return next == ParcelEntryStatus.ACTIVE || next == ParcelEntryStatus.SUSPENDED
            ParcelEntryStatus.ACTIVE -> {
                when (next) {
                    ParcelEntryStatus.SUSPENDED, ParcelEntryStatus.CLOSED -> return true
                }
                return false
            }
            ParcelEntryStatus.SUSPENDED -> return next == ParcelEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ParcelEntryStatus) {
        if (!canMoveTo(next)) {
            error("parcel entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): ParcelEntry {
        return ParcelEntry(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            description = description,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel entry in status draft. */
fun newParcelEntry(code: String, label: String): ParcelEntry {
    return ParcelEntry(
        code = code,
        label = label,
        durationMinutes = 0,
        description = "",
        graceDays = 0,
        status = ParcelEntryStatus.DRAFT,
    )
}
