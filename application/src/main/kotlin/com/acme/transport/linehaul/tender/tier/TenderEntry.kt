package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender entry is in its life cycle. */
enum class TenderEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tender entry and its current status. */
data class TenderEntry(
    var code: String,
    var title: String,
    var sequence: Int,
    var rate: Double,
    var country: String,
    var minimum: Double,
    var pallets: Int,
    var attempts: Int,
    var timezone: String,
    var ratio: Double,
    var status: TenderEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderEntryStatus): Boolean {
        when (status) {
            TenderEntryStatus.OPEN -> return next == TenderEntryStatus.RESOLVED
            TenderEntryStatus.IN_REVIEW -> return next == TenderEntryStatus.RESOLVED || next == TenderEntryStatus.ARCHIVED
            TenderEntryStatus.RESOLVED -> return next == TenderEntryStatus.REOPENED || next == TenderEntryStatus.ARCHIVED
            TenderEntryStatus.REOPENED -> {
                when (next) {
                    TenderEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderEntryStatus) {
        if (!canMoveTo(next)) {
            error("tender entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tender entry"} $code"

    fun netVolume(): Double = round(ratio * attempts.toDouble() * 100) / 100

    fun snapshot(): TenderEntry {
        return TenderEntry(
            code = code,
            title = title,
            sequence = sequence,
            rate = rate,
            country = country,
            minimum = minimum,
            pallets = pallets,
            attempts = attempts,
            timezone = timezone,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tender entry in status open. */
fun newTenderEntry(code: String, title: String): TenderEntry {
    return TenderEntry(
        code = code,
        title = title,
        sequence = 0,
        rate = 0.0,
        country = "",
        minimum = 0.0,
        pallets = 0,
        attempts = 0,
        timezone = "",
        ratio = 0.0,
        status = TenderEntryStatus.OPEN,
    )
}
