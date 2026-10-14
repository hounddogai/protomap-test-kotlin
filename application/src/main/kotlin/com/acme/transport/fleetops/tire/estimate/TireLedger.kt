package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire ledger is in its life cycle. */
enum class TireLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tire ledger, with what the planners need to act on it. */
data class TireLedger(
    var code: String,
    var name: String,
    var threshold: Double,
    var unit: String,
    var updatedAt: Int,
    var status: TireLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireLedgerStatus): Boolean {
        when (status) {
            TireLedgerStatus.CREATED -> {
                when (next) {
                    TireLedgerStatus.PUBLISHED -> return true
                }
                return false
            }
            TireLedgerStatus.VALIDATED -> {
                when (next) {
                    TireLedgerStatus.RETIRED -> return true
                }
                return false
            }
            TireLedgerStatus.PUBLISHED -> {
                when (next) {
                    TireLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireLedgerStatus) {
        if (!canMoveTo(next)) {
            error("tire ledger $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tire ledger"} $code"

    fun weightedTotal(): Double = round((updatedAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): TireLedger {
        return TireLedger(
            code = code,
            name = name,
            threshold = threshold,
            unit = unit,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tire ledger in status created. */
fun newTireLedger(code: String, name: String): TireLedger {
    return TireLedger(
        code = code,
        name = name,
        threshold = 0.0,
        unit = "",
        updatedAt = 0,
        status = TireLedgerStatus.CREATED,
    )
}
