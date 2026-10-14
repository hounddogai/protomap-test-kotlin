package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rate card entry is in its life cycle. */
enum class StrictRateCardEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict rate card entry as the rate card team keeps it. */
data class StrictRateCardEntry(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var approved: Boolean,
    var startsAt: Int,
    var dock: String,
    var updatedAt: Int,
    var ceiling: Double,
    var odometer: Int,
    var sequence: Int,
    var ownerId: String,
    var status: StrictRateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardEntryStatus): Boolean {
        when (status) {
            StrictRateCardEntryStatus.DRAFT -> return next == StrictRateCardEntryStatus.SUSPENDED
            StrictRateCardEntryStatus.PENDING -> return next == StrictRateCardEntryStatus.ACTIVE || next == StrictRateCardEntryStatus.CLOSED
            StrictRateCardEntryStatus.ACTIVE -> {
                when (next) {
                    StrictRateCardEntryStatus.SUSPENDED, StrictRateCardEntryStatus.CLOSED -> return true
                }
                return false
            }
            StrictRateCardEntryStatus.SUSPENDED -> return next == StrictRateCardEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictRateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round(startsAt.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): StrictRateCardEntry {
        return StrictRateCardEntry(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            approved = approved,
            startsAt = startsAt,
            dock = dock,
            updatedAt = updatedAt,
            ceiling = ceiling,
            odometer = odometer,
            sequence = sequence,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict rate card entry in status draft. */
fun newStrictRateCardEntry(code: String, title: String): StrictRateCardEntry {
    return StrictRateCardEntry(
        code = code,
        title = title,
        etaMinutes = 0,
        approved = false,
        startsAt = 0,
        dock = "",
        updatedAt = 0,
        ceiling = 0.0,
        odometer = 0,
        sequence = 0,
        ownerId = "",
        status = StrictRateCardEntryStatus.DRAFT,
    )
}
