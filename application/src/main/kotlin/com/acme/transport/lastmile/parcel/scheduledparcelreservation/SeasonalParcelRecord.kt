package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal parcel record is in its life cycle. */
enum class SeasonalParcelRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal parcel record as the parcel team keeps it. */
data class SeasonalParcelRecord(
    var code: String,
    var label: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var graceDays: Int,
    var startsAt: Int,
    var rate: Double,
    var pinned: Boolean,
    var active: Boolean,
    var contactEmail: String,
    var locale: String,
    var status: SeasonalParcelRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalParcelRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalParcelRecordStatus): Boolean {
        when (status) {
            SeasonalParcelRecordStatus.OPEN -> {
                when (next) {
                    SeasonalParcelRecordStatus.IN_REVIEW, SeasonalParcelRecordStatus.RESOLVED -> return true
                }
                return false
            }
            SeasonalParcelRecordStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalParcelRecordStatus.RESOLVED, SeasonalParcelRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalParcelRecordStatus.RESOLVED -> return next == SeasonalParcelRecordStatus.ARCHIVED
            SeasonalParcelRecordStatus.REOPENED -> return next == SeasonalParcelRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalParcelRecordStatus) {
        if (!canMoveTo(next)) {
            error("seasonal parcel record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"seasonal parcel record"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((graceDays.toDouble() + rate) * 100) / 100

    fun snapshot(): SeasonalParcelRecord {
        return SeasonalParcelRecord(
            code = code,
            label = label,
            pallets = pallets,
            tags = tags,
            graceDays = graceDays,
            startsAt = startsAt,
            rate = rate,
            pinned = pinned,
            active = active,
            contactEmail = contactEmail,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal parcel record in status open. */
fun newSeasonalParcelRecord(code: String, label: String): SeasonalParcelRecord {
    return SeasonalParcelRecord(
        code = code,
        label = label,
        pallets = 0,
        tags = mutableListOf<String>(),
        graceDays = 0,
        startsAt = 0,
        rate = 0.0,
        pinned = false,
        active = false,
        contactEmail = "",
        locale = "",
        status = SeasonalParcelRecordStatus.OPEN,
    )
}
