package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient attempt ledger records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a lenient attempt ledger is in its life cycle. */
enum class LenientAttemptLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient attempt ledger, with what the planners need to act on it. */
data class LenientAttemptLedger(
    var code: String,
    var title: String,
    var contactEmail: String,
    var etaMinutes: Int,
    var version: Int,
    var endsAt: Int,
    var channel: String,
    var windowDays: Int,
    var shiftCode: String,
    var capacity: Int,
    var fuelLiters: Double,
    var status: LenientAttemptLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAttemptLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAttemptLedgerStatus): Boolean {
        when (status) {
            LenientAttemptLedgerStatus.PLANNED -> return next == LenientAttemptLedgerStatus.SCHEDULED || next == LenientAttemptLedgerStatus.CANCELLED
            LenientAttemptLedgerStatus.SCHEDULED -> {
                when (next) {
                    LenientAttemptLedgerStatus.COMPLETED, LenientAttemptLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            LenientAttemptLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    LenientAttemptLedgerStatus.COMPLETED, LenientAttemptLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            LenientAttemptLedgerStatus.COMPLETED -> {
                when (next) {
                    LenientAttemptLedgerStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAttemptLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient attempt ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"lenient attempt ledger"} $code"

    fun weightedVolume(): Double = round((endsAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): LenientAttemptLedger {
        return LenientAttemptLedger(
            code = code,
            title = title,
            contactEmail = contactEmail,
            etaMinutes = etaMinutes,
            version = version,
            endsAt = endsAt,
            channel = channel,
            windowDays = windowDays,
            shiftCode = shiftCode,
            capacity = capacity,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient attempt ledger in status planned. */
fun newLenientAttemptLedger(code: String, title: String): LenientAttemptLedger {
    return LenientAttemptLedger(
        code = code,
        title = title,
        contactEmail = "",
        etaMinutes = 0,
        version = 0,
        endsAt = 0,
        channel = "",
        windowDays = 0,
        shiftCode = "",
        capacity = 0,
        fuelLiters = 0.0,
        status = LenientAttemptLedgerStatus.PLANNED,
    )
}
