package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped accessorial entry records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a capped accessorial entry is in its life cycle. */
enum class CappedAccessorialEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capped accessorial entry, with what the planners need to act on it. */
data class CappedAccessorialEntry(
    var code: String,
    var name: String,
    var source: String,
    var graceDays: Int,
    var quantity: Int,
    var ownerId: String,
    var status: CappedAccessorialEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAccessorialEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAccessorialEntryStatus): Boolean {
        when (status) {
            CappedAccessorialEntryStatus.NEW -> return next == CappedAccessorialEntryStatus.DONE
            CappedAccessorialEntryStatus.QUEUED -> {
                when (next) {
                    CappedAccessorialEntryStatus.DONE, CappedAccessorialEntryStatus.FAILED -> return true
                }
                return false
            }
            CappedAccessorialEntryStatus.RUNNING -> {
                when (next) {
                    CappedAccessorialEntryStatus.DONE, CappedAccessorialEntryStatus.FAILED -> return true
                }
                return false
            }
            CappedAccessorialEntryStatus.DONE -> {
                when (next) {
                    CappedAccessorialEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAccessorialEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped accessorial entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round((quantity.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedAccessorialEntry {
        return CappedAccessorialEntry(
            code = code,
            name = name,
            source = source,
            graceDays = graceDays,
            quantity = quantity,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped accessorial entry in status new. */
fun newCappedAccessorialEntry(code: String, name: String): CappedAccessorialEntry {
    return CappedAccessorialEntry(
        code = code,
        name = name,
        source = "",
        graceDays = 0,
        quantity = 0,
        ownerId = "",
        status = CappedAccessorialEntryStatus.NEW,
    )
}
