package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker ledger is in its life cycle. */
enum class LockerLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A locker ledger. Its code is unique within a tenant. */
data class LockerLedger(
    var code: String,
    var title: String,
    var weight: Double,
    var phone: String,
    var quantity: Int,
    var ceiling: Double,
    var status: LockerLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerLedgerStatus): Boolean {
        when (status) {
            LockerLedgerStatus.IDLE -> return next == LockerLedgerStatus.READY
            LockerLedgerStatus.WARMING -> {
                when (next) {
                    LockerLedgerStatus.STOPPED -> return true
                }
                return false
            }
            LockerLedgerStatus.READY -> {
                when (next) {
                    LockerLedgerStatus.DRAINING, LockerLedgerStatus.STOPPED -> return true
                }
                return false
            }
            LockerLedgerStatus.DRAINING -> return next == LockerLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LockerLedgerStatus) {
        if (!canMoveTo(next)) {
            error("locker ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker ledger"} $code"

    fun netExposure(): Double = round((quantity.toDouble() - weight) * 100) / 100

    fun snapshot(): LockerLedger {
        return LockerLedger(
            code = code,
            title = title,
            weight = weight,
            phone = phone,
            quantity = quantity,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker ledger in status idle. */
fun newLockerLedger(code: String, title: String): LockerLedger {
    return LockerLedger(
        code = code,
        title = title,
        weight = 0.0,
        phone = "",
        quantity = 0,
        ceiling = 0.0,
        status = LockerLedgerStatus.IDLE,
    )
}
