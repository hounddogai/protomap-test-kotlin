package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation digest records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation digest is in its life cycle. */
enum class SortationDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A sortation digest as the sortation team keeps it. */
data class SortationDigest(
    var code: String,
    var title: String,
    var country: String,
    var cost: Double,
    var endsAt: Int,
    var budget: Double,
    var carrier: String,
    var status: SortationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationDigestStatus): Boolean {
        when (status) {
            SortationDigestStatus.OPEN -> return next == SortationDigestStatus.IN_REVIEW || next == SortationDigestStatus.ARCHIVED
            SortationDigestStatus.IN_REVIEW -> {
                when (next) {
                    SortationDigestStatus.RESOLVED, SortationDigestStatus.REOPENED -> return true
                }
                return false
            }
            SortationDigestStatus.RESOLVED -> return next == SortationDigestStatus.REOPENED
            SortationDigestStatus.REOPENED -> return next == SortationDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SortationDigestStatus) {
        if (!canMoveTo(next)) {
            error("sortation digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round(cost * budget * 100) / 100

    fun snapshot(): SortationDigest {
        return SortationDigest(
            code = code,
            title = title,
            country = country,
            cost = cost,
            endsAt = endsAt,
            budget = budget,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a sortation digest in status open. */
fun newSortationDigest(code: String, title: String): SortationDigest {
    return SortationDigest(
        code = code,
        title = title,
        country = "",
        cost = 0.0,
        endsAt = 0,
        budget = 0.0,
        carrier = "",
        status = SortationDigestStatus.OPEN,
    )
}
