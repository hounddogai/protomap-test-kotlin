package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.text.normalize

// The pending hub ledger records of network's hub work, and the rules, stores, and calculations around them.

/** Where a pending hub ledger is in its life cycle. */
enum class PendingHubLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One pending hub ledger, with what the planners need to act on it. */
data class PendingHubLedger(
    var code: String,
    var label: String,
    var locale: String,
    var unit: String,
    var phone: String,
    var score: Double,
    var status: PendingHubLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHubLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHubLedgerStatus): Boolean {
        when (status) {
            PendingHubLedgerStatus.PROPOSED -> {
                when (next) {
                    PendingHubLedgerStatus.APPROVED, PendingHubLedgerStatus.REJECTED -> return true
                }
                return false
            }
            PendingHubLedgerStatus.APPROVED -> {
                when (next) {
                    PendingHubLedgerStatus.REJECTED, PendingHubLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingHubLedgerStatus.REJECTED -> {
                when (next) {
                    PendingHubLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHubLedgerStatus) {
        if (!canMoveTo(next)) {
            error("pending hub ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = score

    fun snapshot(): PendingHubLedger {
        return PendingHubLedger(
            code = code,
            label = label,
            locale = locale,
            unit = unit,
            phone = phone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending hub ledger in status proposed. */
fun newPendingHubLedger(code: String, label: String): PendingHubLedger {
    return PendingHubLedger(
        code = code,
        label = label,
        locale = "",
        unit = "",
        phone = "",
        score = 0.0,
        status = PendingHubLedgerStatus.PROPOSED,
    )
}
