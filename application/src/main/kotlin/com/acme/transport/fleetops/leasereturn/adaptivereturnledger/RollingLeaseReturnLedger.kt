package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling lease return ledger records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a rolling lease return ledger is in its life cycle. */
enum class RollingLeaseReturnLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling lease return ledger, with what the planners need to act on it. */
data class RollingLeaseReturnLedger(
    var code: String,
    var label: String,
    var minimum: Double,
    var loadKg: Double,
    var rate: Double,
    var weight: Double,
    var status: RollingLeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLeaseReturnLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLeaseReturnLedgerStatus): Boolean {
        when (status) {
            RollingLeaseReturnLedgerStatus.PLANNED -> {
                when (next) {
                    RollingLeaseReturnLedgerStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingLeaseReturnLedgerStatus.SCHEDULED -> {
                when (next) {
                    RollingLeaseReturnLedgerStatus.IN_PROGRESS, RollingLeaseReturnLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            RollingLeaseReturnLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    RollingLeaseReturnLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            RollingLeaseReturnLedgerStatus.COMPLETED -> return next == RollingLeaseReturnLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingLeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling lease return ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling lease return ledger"} $code"

    fun effectiveVolume(): Double = round(minimum * rate * 100) / 100

    fun snapshot(): RollingLeaseReturnLedger {
        return RollingLeaseReturnLedger(
            code = code,
            label = label,
            minimum = minimum,
            loadKg = loadKg,
            rate = rate,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling lease return ledger in status planned. */
fun newRollingLeaseReturnLedger(code: String, label: String): RollingLeaseReturnLedger {
    return RollingLeaseReturnLedger(
        code = code,
        label = label,
        minimum = 0.0,
        loadKg = 0.0,
        rate = 0.0,
        weight = 0.0,
        status = RollingLeaseReturnLedgerStatus.PLANNED,
    )
}
