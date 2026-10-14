package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.text.normalize

/** Where a fallback accessorial ledger is in its life cycle. */
enum class FallbackAccessorialLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fallback accessorial ledger and its current status. */
data class FallbackAccessorialLedger(
    var code: String,
    var label: String,
    var ownerId: String,
    var odometer: Int,
    var phone: String,
    var status: FallbackAccessorialLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAccessorialLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAccessorialLedgerStatus): Boolean {
        when (status) {
            FallbackAccessorialLedgerStatus.DRAFT -> return next == FallbackAccessorialLedgerStatus.PENDING || next == FallbackAccessorialLedgerStatus.SUSPENDED
            FallbackAccessorialLedgerStatus.PENDING -> {
                when (next) {
                    FallbackAccessorialLedgerStatus.SUSPENDED, FallbackAccessorialLedgerStatus.CLOSED -> return true
                }
                return false
            }
            FallbackAccessorialLedgerStatus.ACTIVE -> {
                when (next) {
                    FallbackAccessorialLedgerStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackAccessorialLedgerStatus.SUSPENDED -> {
                when (next) {
                    FallbackAccessorialLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAccessorialLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback accessorial ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): FallbackAccessorialLedger {
        return FallbackAccessorialLedger(
            code = code,
            label = label,
            ownerId = ownerId,
            odometer = odometer,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback accessorial ledger in status draft. */
fun newFallbackAccessorialLedger(code: String, label: String): FallbackAccessorialLedger {
    return FallbackAccessorialLedger(
        code = code,
        label = label,
        ownerId = "",
        odometer = 0,
        phone = "",
        status = FallbackAccessorialLedgerStatus.DRAFT,
    )
}
