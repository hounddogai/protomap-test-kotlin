package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel ledger records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel ledger is in its life cycle. */
enum class ParcelLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One parcel ledger, with what the planners need to act on it. */
data class ParcelLedger(
    var code: String,
    var label: String,
    var archived: Boolean,
    var stopCount: Int,
    var ceiling: Double,
    var amount: Double,
    var name: String,
    var depot: String,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var status: ParcelLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelLedgerStatus): Boolean {
        when (status) {
            ParcelLedgerStatus.CREATED -> return next == ParcelLedgerStatus.RETIRED
            ParcelLedgerStatus.VALIDATED -> return next == ParcelLedgerStatus.PUBLISHED || next == ParcelLedgerStatus.RETIRED
            ParcelLedgerStatus.PUBLISHED -> {
                when (next) {
                    ParcelLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelLedgerStatus) {
        if (!canMoveTo(next)) {
            error("parcel ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round(durationMinutes.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): ParcelLedger {
        return ParcelLedger(
            code = code,
            label = label,
            archived = archived,
            stopCount = stopCount,
            ceiling = ceiling,
            amount = amount,
            name = name,
            depot = depot,
            durationMinutes = durationMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a parcel ledger in status created. */
fun newParcelLedger(code: String, label: String): ParcelLedger {
    return ParcelLedger(
        code = code,
        label = label,
        archived = false,
        stopCount = 0,
        ceiling = 0.0,
        amount = 0.0,
        name = "",
        depot = "",
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        status = ParcelLedgerStatus.CREATED,
    )
}
