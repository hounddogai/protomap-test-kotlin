package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary cutoff entry records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a primary cutoff entry is in its life cycle. */
enum class PrimaryCutoffEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary cutoff entry as the cutoff team keeps it. */
data class PrimaryCutoffEntry(
    var code: String,
    var title: String,
    var channel: String,
    var rate: Double,
    var sequence: Int,
    var endsAt: Int,
    var routeCode: String,
    var approved: Boolean,
    var status: PrimaryCutoffEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCutoffEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCutoffEntryStatus): Boolean {
        when (status) {
            PrimaryCutoffEntryStatus.OPEN -> {
                when (next) {
                    PrimaryCutoffEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryCutoffEntryStatus.IN_REVIEW -> return next == PrimaryCutoffEntryStatus.ARCHIVED
            PrimaryCutoffEntryStatus.RESOLVED -> {
                when (next) {
                    PrimaryCutoffEntryStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCutoffEntryStatus.REOPENED -> {
                when (next) {
                    PrimaryCutoffEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCutoffEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary cutoff entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((rate - endsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCutoffEntry {
        return PrimaryCutoffEntry(
            code = code,
            title = title,
            channel = channel,
            rate = rate,
            sequence = sequence,
            endsAt = endsAt,
            routeCode = routeCode,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary cutoff entry in status open. */
fun newPrimaryCutoffEntry(code: String, title: String): PrimaryCutoffEntry {
    return PrimaryCutoffEntry(
        code = code,
        title = title,
        channel = "",
        rate = 0.0,
        sequence = 0,
        endsAt = 0,
        routeCode = "",
        approved = false,
        status = PrimaryCutoffEntryStatus.OPEN,
    )
}
