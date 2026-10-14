package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending service plan ledger is in its life cycle. */
enum class PendingServicePlanLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending service plan ledger. Its code is unique within a tenant. */
data class PendingServicePlanLedger(
    var code: String,
    var label: String,
    var driverId: String,
    var attempts: Int,
    var ceiling: Double,
    var score: Double,
    var locale: String,
    var active: Boolean,
    var routeCode: String,
    var priority: Int,
    var source: String,
    var status: PendingServicePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServicePlanLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServicePlanLedgerStatus): Boolean {
        when (status) {
            PendingServicePlanLedgerStatus.IDLE -> {
                when (next) {
                    PendingServicePlanLedgerStatus.READY -> return true
                }
                return false
            }
            PendingServicePlanLedgerStatus.WARMING -> return next == PendingServicePlanLedgerStatus.DRAINING || next == PendingServicePlanLedgerStatus.STOPPED
            PendingServicePlanLedgerStatus.READY -> return next == PendingServicePlanLedgerStatus.DRAINING || next == PendingServicePlanLedgerStatus.STOPPED
            PendingServicePlanLedgerStatus.DRAINING -> return next == PendingServicePlanLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingServicePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("pending service plan ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending service plan ledger"} $code"

    fun netTotal(): Double = round((attempts.toDouble() + score) * 100) / 100

    fun snapshot(): PendingServicePlanLedger {
        return PendingServicePlanLedger(
            code = code,
            label = label,
            driverId = driverId,
            attempts = attempts,
            ceiling = ceiling,
            score = score,
            locale = locale,
            active = active,
            routeCode = routeCode,
            priority = priority,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending service plan ledger in status idle. */
fun newPendingServicePlanLedger(code: String, label: String): PendingServicePlanLedger {
    return PendingServicePlanLedger(
        code = code,
        label = label,
        driverId = "",
        attempts = 0,
        ceiling = 0.0,
        score = 0.0,
        locale = "",
        active = false,
        routeCode = "",
        priority = 0,
        source = "",
        status = PendingServicePlanLedgerStatus.IDLE,
    )
}
