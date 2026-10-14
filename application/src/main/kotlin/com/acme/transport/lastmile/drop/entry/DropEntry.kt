package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.normalize

// The drop entry records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop entry is in its life cycle. */
enum class DropEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a drop entry and its current status. */
data class DropEntry(
    var code: String,
    var title: String,
    var region: String,
    var reviewer: String,
    var currency: String,
    var carrier: String,
    var lane: String,
    var quantity: Int,
    var status: DropEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropEntryStatus): Boolean {
        when (status) {
            DropEntryStatus.DRAFT -> return next == DropEntryStatus.ACTIVE || next == DropEntryStatus.SUSPENDED
            DropEntryStatus.PENDING -> {
                when (next) {
                    DropEntryStatus.SUSPENDED, DropEntryStatus.CLOSED -> return true
                }
                return false
            }
            DropEntryStatus.ACTIVE -> return next == DropEntryStatus.CLOSED
            DropEntryStatus.SUSPENDED -> return next == DropEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DropEntryStatus) {
        if (!canMoveTo(next)) {
            error("drop entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DropEntry {
        return DropEntry(
            code = code,
            title = title,
            region = region,
            reviewer = reviewer,
            currency = currency,
            carrier = carrier,
            lane = lane,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a drop entry in status draft. */
fun newDropEntry(code: String, title: String): DropEntry {
    return DropEntry(
        code = code,
        title = title,
        region = "",
        reviewer = "",
        currency = "",
        carrier = "",
        lane = "",
        quantity = 0,
        status = DropEntryStatus.DRAFT,
    )
}
