package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global redelivery ledger records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a global redelivery ledger is in its life cycle. */
enum class GlobalRedeliveryLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global redelivery ledger. Its code is unique within a tenant. */
data class GlobalRedeliveryLedger(
    var code: String,
    var name: String,
    var score: Double,
    var depot: String,
    var threshold: Double,
    var status: GlobalRedeliveryLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRedeliveryLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRedeliveryLedgerStatus): Boolean {
        when (status) {
            GlobalRedeliveryLedgerStatus.CREATED -> {
                when (next) {
                    GlobalRedeliveryLedgerStatus.PUBLISHED, GlobalRedeliveryLedgerStatus.RETIRED -> return true
                }
                return false
            }
            GlobalRedeliveryLedgerStatus.VALIDATED -> {
                when (next) {
                    GlobalRedeliveryLedgerStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalRedeliveryLedgerStatus.PUBLISHED -> return next == GlobalRedeliveryLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalRedeliveryLedgerStatus) {
        if (!canMoveTo(next)) {
            error("global redelivery ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((threshold + score) * 100) / 100

    fun snapshot(): GlobalRedeliveryLedger {
        return GlobalRedeliveryLedger(
            code = code,
            name = name,
            score = score,
            depot = depot,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global redelivery ledger in status created. */
fun newGlobalRedeliveryLedger(code: String, name: String): GlobalRedeliveryLedger {
    return GlobalRedeliveryLedger(
        code = code,
        name = name,
        score = 0.0,
        depot = "",
        threshold = 0.0,
        status = GlobalRedeliveryLedgerStatus.CREATED,
    )
}
