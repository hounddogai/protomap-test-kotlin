package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling service plan ledger is in its life cycle. */
enum class RollingServicePlanLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling service plan ledger, with what the planners need to act on it. */
data class RollingServicePlanLedger(
    var code: String,
    var name: String,
    var retries: Int,
    var currency: String,
    var batchSize: Int,
    var floorValue: Double,
    var status: RollingServicePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingServicePlanLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingServicePlanLedgerStatus): Boolean {
        when (status) {
            RollingServicePlanLedgerStatus.OPEN -> {
                when (next) {
                    RollingServicePlanLedgerStatus.RESOLVED -> return true
                }
                return false
            }
            RollingServicePlanLedgerStatus.IN_REVIEW -> return next == RollingServicePlanLedgerStatus.RESOLVED
            RollingServicePlanLedgerStatus.RESOLVED -> {
                when (next) {
                    RollingServicePlanLedgerStatus.REOPENED, RollingServicePlanLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingServicePlanLedgerStatus.REOPENED -> return next == RollingServicePlanLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingServicePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling service plan ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling service plan ledger"} $code"

    fun effectiveLoad(): Double = round(floorValue * batchSize.toDouble() * 100) / 100

    fun snapshot(): RollingServicePlanLedger {
        return RollingServicePlanLedger(
            code = code,
            name = name,
            retries = retries,
            currency = currency,
            batchSize = batchSize,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling service plan ledger in status open. */
fun newRollingServicePlanLedger(code: String, name: String): RollingServicePlanLedger {
    return RollingServicePlanLedger(
        code = code,
        name = name,
        retries = 0,
        currency = "",
        batchSize = 0,
        floorValue = 0.0,
        status = RollingServicePlanLedgerStatus.OPEN,
    )
}
