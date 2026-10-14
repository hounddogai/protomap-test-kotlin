package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient appointment ledger records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a lenient appointment ledger is in its life cycle. */
enum class LenientAppointmentLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient appointment ledger as the appointment team keeps it. */
data class LenientAppointmentLedger(
    var code: String,
    var name: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var floorValue: Double,
    var startsAt: Int,
    var budget: Double,
    var status: LenientAppointmentLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAppointmentLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAppointmentLedgerStatus): Boolean {
        when (status) {
            LenientAppointmentLedgerStatus.OPEN -> {
                when (next) {
                    LenientAppointmentLedgerStatus.REOPENED, LenientAppointmentLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientAppointmentLedgerStatus.IN_REVIEW -> {
                when (next) {
                    LenientAppointmentLedgerStatus.RESOLVED -> return true
                }
                return false
            }
            LenientAppointmentLedgerStatus.RESOLVED -> return next == LenientAppointmentLedgerStatus.REOPENED || next == LenientAppointmentLedgerStatus.ARCHIVED
            LenientAppointmentLedgerStatus.REOPENED -> return next == LenientAppointmentLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientAppointmentLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient appointment ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((budget + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientAppointmentLedger {
        return LenientAppointmentLedger(
            code = code,
            name = name,
            shiftCode = shiftCode,
            tags = tags,
            floorValue = floorValue,
            startsAt = startsAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient appointment ledger in status open. */
fun newLenientAppointmentLedger(code: String, name: String): LenientAppointmentLedger {
    return LenientAppointmentLedger(
        code = code,
        name = name,
        shiftCode = "",
        tags = mutableListOf<String>(),
        floorValue = 0.0,
        startsAt = 0,
        budget = 0.0,
        status = LenientAppointmentLedgerStatus.OPEN,
    )
}
