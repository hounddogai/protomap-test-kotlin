package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery entry records of lastmile's redelivery work, and the rules, stores, and calculations around
// them.

/** Where a redelivery entry is in its life cycle. */
enum class RedeliveryEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A redelivery entry as the redelivery team keeps it. */
data class RedeliveryEntry(
    var code: String,
    var title: String,
    var stopCount: Int,
    var locale: String,
    var amount: Double,
    var status: RedeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryEntryStatus): Boolean {
        when (status) {
            RedeliveryEntryStatus.PROPOSED -> {
                when (next) {
                    RedeliveryEntryStatus.APPROVED, RedeliveryEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            RedeliveryEntryStatus.APPROVED -> {
                when (next) {
                    RedeliveryEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            RedeliveryEntryStatus.REJECTED -> {
                when (next) {
                    RedeliveryEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("redelivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((stopCount.toDouble() + amount) * 100) / 100

    fun snapshot(): RedeliveryEntry {
        return RedeliveryEntry(
            code = code,
            title = title,
            stopCount = stopCount,
            locale = locale,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a redelivery entry in status proposed. */
fun newRedeliveryEntry(code: String, title: String): RedeliveryEntry {
    return RedeliveryEntry(
        code = code,
        title = title,
        stopCount = 0,
        locale = "",
        amount = 0.0,
        status = RedeliveryEntryStatus.PROPOSED,
    )
}
