package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped parcel entry is in its life cycle. */
enum class CappedParcelEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capped parcel entry, with what the planners need to act on it. */
data class CappedParcelEntry(
    var code: String,
    var name: String,
    var depot: String,
    var approved: Boolean,
    var retries: Int,
    var pallets: Int,
    var pinned: Boolean,
    var minimum: Double,
    var status: CappedParcelEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedParcelEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedParcelEntryStatus): Boolean {
        when (status) {
            CappedParcelEntryStatus.PROPOSED -> return next == CappedParcelEntryStatus.WITHDRAWN
            CappedParcelEntryStatus.APPROVED -> {
                when (next) {
                    CappedParcelEntryStatus.REJECTED -> return true
                }
                return false
            }
            CappedParcelEntryStatus.REJECTED -> {
                when (next) {
                    CappedParcelEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedParcelEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped parcel entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedValue(): Double = round((minimum + retries.toDouble()) * 100) / 100

    fun snapshot(): CappedParcelEntry {
        return CappedParcelEntry(
            code = code,
            name = name,
            depot = depot,
            approved = approved,
            retries = retries,
            pallets = pallets,
            pinned = pinned,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped parcel entry in status proposed. */
fun newCappedParcelEntry(code: String, name: String): CappedParcelEntry {
    return CappedParcelEntry(
        code = code,
        name = name,
        depot = "",
        approved = false,
        retries = 0,
        pallets = 0,
        pinned = false,
        minimum = 0.0,
        status = CappedParcelEntryStatus.PROPOSED,
    )
}
