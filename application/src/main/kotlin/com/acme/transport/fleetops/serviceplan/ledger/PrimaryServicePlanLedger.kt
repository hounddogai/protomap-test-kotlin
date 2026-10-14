package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service plan ledger is in its life cycle. */
enum class PrimaryServicePlanLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary service plan ledger and its current status. */
data class PrimaryServicePlanLedger(
    var code: String,
    var label: String,
    var region: String,
    var startsAt: Int,
    var description: String,
    var endsAt: Int,
    var floorValue: Double,
    var status: PrimaryServicePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanLedgerStatus): Boolean {
        when (status) {
            PrimaryServicePlanLedgerStatus.PROPOSED -> {
                when (next) {
                    PrimaryServicePlanLedgerStatus.APPROVED -> return true
                }
                return false
            }
            PrimaryServicePlanLedgerStatus.APPROVED -> return next == PrimaryServicePlanLedgerStatus.REJECTED || next == PrimaryServicePlanLedgerStatus.WITHDRAWN
            PrimaryServicePlanLedgerStatus.REJECTED -> {
                when (next) {
                    PrimaryServicePlanLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((floorValue - endsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryServicePlanLedger {
        return PrimaryServicePlanLedger(
            code = code,
            label = label,
            region = region,
            startsAt = startsAt,
            description = description,
            endsAt = endsAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary service plan ledger in status proposed. */
fun newPrimaryServicePlanLedger(code: String, label: String): PrimaryServicePlanLedger {
    return PrimaryServicePlanLedger(
        code = code,
        label = label,
        region = "",
        startsAt = 0,
        description = "",
        endsAt = 0,
        floorValue = 0.0,
        status = PrimaryServicePlanLedgerStatus.PROPOSED,
    )
}
