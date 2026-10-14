package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient roster ledger is in its life cycle. */
enum class LenientRosterLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient roster ledger, with what the planners need to act on it. */
data class LenientRosterLedger(
    var code: String,
    var name: String,
    var retries: Int,
    var floorValue: Double,
    var fuelLiters: Double,
    var channel: String,
    var contactEmail: String,
    var ratio: Double,
    var pallets: Int,
    var pinned: Boolean,
    var attempts: Int,
    var status: LenientRosterLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRosterLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRosterLedgerStatus): Boolean {
        when (status) {
            LenientRosterLedgerStatus.CREATED -> return next == LenientRosterLedgerStatus.VALIDATED || next == LenientRosterLedgerStatus.RETIRED
            LenientRosterLedgerStatus.VALIDATED -> return next == LenientRosterLedgerStatus.PUBLISHED || next == LenientRosterLedgerStatus.RETIRED
            LenientRosterLedgerStatus.PUBLISHED -> return next == LenientRosterLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRosterLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient roster ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round(fuelLiters * floorValue * 100) / 100

    fun snapshot(): LenientRosterLedger {
        return LenientRosterLedger(
            code = code,
            name = name,
            retries = retries,
            floorValue = floorValue,
            fuelLiters = fuelLiters,
            channel = channel,
            contactEmail = contactEmail,
            ratio = ratio,
            pallets = pallets,
            pinned = pinned,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient roster ledger in status created. */
fun newLenientRosterLedger(code: String, name: String): LenientRosterLedger {
    return LenientRosterLedger(
        code = code,
        name = name,
        retries = 0,
        floorValue = 0.0,
        fuelLiters = 0.0,
        channel = "",
        contactEmail = "",
        ratio = 0.0,
        pallets = 0,
        pinned = false,
        attempts = 0,
        status = LenientRosterLedgerStatus.CREATED,
    )
}
