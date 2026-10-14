package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional lease return entry records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a regional lease return entry is in its life cycle. */
enum class RegionalLeaseReturnEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional lease return entry, with what the planners need to act on it. */
data class RegionalLeaseReturnEntry(
    var code: String,
    var name: String,
    var graceDays: Int,
    var ratio: Double,
    var dock: String,
    var title: String,
    var status: RegionalLeaseReturnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnEntryStatus): Boolean {
        when (status) {
            RegionalLeaseReturnEntryStatus.OPEN -> return next == RegionalLeaseReturnEntryStatus.RESOLVED
            RegionalLeaseReturnEntryStatus.IN_REVIEW -> {
                when (next) {
                    RegionalLeaseReturnEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalLeaseReturnEntryStatus.RESOLVED -> return next == RegionalLeaseReturnEntryStatus.ARCHIVED
            RegionalLeaseReturnEntryStatus.REOPENED -> return next == RegionalLeaseReturnEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnEntryStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((graceDays.toDouble() - ratio) * 100) / 100

    fun snapshot(): RegionalLeaseReturnEntry {
        return RegionalLeaseReturnEntry(
            code = code,
            name = name,
            graceDays = graceDays,
            ratio = ratio,
            dock = dock,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional lease return entry in status open. */
fun newRegionalLeaseReturnEntry(code: String, name: String): RegionalLeaseReturnEntry {
    return RegionalLeaseReturnEntry(
        code = code,
        name = name,
        graceDays = 0,
        ratio = 0.0,
        dock = "",
        title = "",
        status = RegionalLeaseReturnEntryStatus.OPEN,
    )
}
