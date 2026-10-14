package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient lease return ledger is in its life cycle. */
enum class LenientLeaseReturnLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lenient lease return ledger, with what the planners need to act on it. */
data class LenientLeaseReturnLedger(
    var code: String,
    var label: String,
    var graceDays: Int,
    var active: Boolean,
    var etaMinutes: Int,
    var pinned: Boolean,
    var description: String,
    var quantity: Int,
    var status: LenientLeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLeaseReturnLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLeaseReturnLedgerStatus): Boolean {
        when (status) {
            LenientLeaseReturnLedgerStatus.NEW -> {
                when (next) {
                    LenientLeaseReturnLedgerStatus.DONE -> return true
                }
                return false
            }
            LenientLeaseReturnLedgerStatus.QUEUED -> {
                when (next) {
                    LenientLeaseReturnLedgerStatus.FAILED -> return true
                }
                return false
            }
            LenientLeaseReturnLedgerStatus.RUNNING -> {
                when (next) {
                    LenientLeaseReturnLedgerStatus.DONE, LenientLeaseReturnLedgerStatus.FAILED -> return true
                }
                return false
            }
            LenientLeaseReturnLedgerStatus.DONE -> {
                when (next) {
                    LenientLeaseReturnLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient lease return ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((etaMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): LenientLeaseReturnLedger {
        return LenientLeaseReturnLedger(
            code = code,
            label = label,
            graceDays = graceDays,
            active = active,
            etaMinutes = etaMinutes,
            pinned = pinned,
            description = description,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient lease return ledger in status new. */
fun newLenientLeaseReturnLedger(code: String, label: String): LenientLeaseReturnLedger {
    return LenientLeaseReturnLedger(
        code = code,
        label = label,
        graceDays = 0,
        active = false,
        etaMinutes = 0,
        pinned = false,
        description = "",
        quantity = 0,
        status = LenientLeaseReturnLedgerStatus.NEW,
    )
}
