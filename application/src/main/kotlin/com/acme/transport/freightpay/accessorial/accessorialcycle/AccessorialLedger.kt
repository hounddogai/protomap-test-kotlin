package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial ledger is in its life cycle. */
enum class AccessorialLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A accessorial ledger as the accessorial team keeps it. */
data class AccessorialLedger(
    var code: String,
    var title: String,
    var createdAt: Int,
    var driverId: String,
    var source: String,
    var ratio: Double,
    var attempts: Int,
    var status: AccessorialLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialLedgerStatus): Boolean {
        when (status) {
            AccessorialLedgerStatus.PROPOSED -> return next == AccessorialLedgerStatus.APPROVED || next == AccessorialLedgerStatus.REJECTED
            AccessorialLedgerStatus.APPROVED -> {
                when (next) {
                    AccessorialLedgerStatus.REJECTED, AccessorialLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            AccessorialLedgerStatus.REJECTED -> {
                when (next) {
                    AccessorialLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialLedgerStatus) {
        if (!canMoveTo(next)) {
            error("accessorial ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"accessorial ledger"} $code"

    fun effectiveValue(): Double = round((ratio + attempts.toDouble()) * 100) / 100

    fun snapshot(): AccessorialLedger {
        return AccessorialLedger(
            code = code,
            title = title,
            createdAt = createdAt,
            driverId = driverId,
            source = source,
            ratio = ratio,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a accessorial ledger in status proposed. */
fun newAccessorialLedger(code: String, title: String): AccessorialLedger {
    return AccessorialLedger(
        code = code,
        title = title,
        createdAt = 0,
        driverId = "",
        source = "",
        ratio = 0.0,
        attempts = 0,
        status = AccessorialLedgerStatus.PROPOSED,
    )
}
