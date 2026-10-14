package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default service plan ledger records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a default service plan ledger is in its life cycle. */
enum class DefaultServicePlanLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default service plan ledger and its current status. */
data class DefaultServicePlanLedger(
    var code: String,
    var label: String,
    var region: String,
    var contactEmail: String,
    var ceiling: Double,
    var attempts: Int,
    var startsAt: Int,
    var sequence: Int,
    var minimum: Double,
    var status: DefaultServicePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServicePlanLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServicePlanLedgerStatus): Boolean {
        when (status) {
            DefaultServicePlanLedgerStatus.REQUESTED -> return next == DefaultServicePlanLedgerStatus.EXPIRED
            DefaultServicePlanLedgerStatus.GRANTED -> return next == DefaultServicePlanLedgerStatus.EXPIRED
            DefaultServicePlanLedgerStatus.REVOKED -> {
                when (next) {
                    DefaultServicePlanLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultServicePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("default service plan ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default service plan ledger"} $code"

    fun projectedValue(): Double = round((attempts.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): DefaultServicePlanLedger {
        return DefaultServicePlanLedger(
            code = code,
            label = label,
            region = region,
            contactEmail = contactEmail,
            ceiling = ceiling,
            attempts = attempts,
            startsAt = startsAt,
            sequence = sequence,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default service plan ledger in status requested. */
fun newDefaultServicePlanLedger(code: String, label: String): DefaultServicePlanLedger {
    return DefaultServicePlanLedger(
        code = code,
        label = label,
        region = "",
        contactEmail = "",
        ceiling = 0.0,
        attempts = 0,
        startsAt = 0,
        sequence = 0,
        minimum = 0.0,
        status = DefaultServicePlanLedgerStatus.REQUESTED,
    )
}
