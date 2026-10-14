package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization ledger records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization ledger is in its life cycle. */
enum class UtilizationLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A utilization ledger as the utilization team keeps it. */
data class UtilizationLedger(
    var code: String,
    var title: String,
    var notes: String,
    var sequence: Int,
    var discount: Double,
    var region: String,
    var startsAt: Int,
    var status: UtilizationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationLedgerStatus): Boolean {
        when (status) {
            UtilizationLedgerStatus.CREATED -> {
                when (next) {
                    UtilizationLedgerStatus.RETIRED -> return true
                }
                return false
            }
            UtilizationLedgerStatus.VALIDATED -> {
                when (next) {
                    UtilizationLedgerStatus.PUBLISHED, UtilizationLedgerStatus.RETIRED -> return true
                }
                return false
            }
            UtilizationLedgerStatus.PUBLISHED -> return next == UtilizationLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: UtilizationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("utilization ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round(discount * startsAt.toDouble() * 100) / 100

    fun snapshot(): UtilizationLedger {
        return UtilizationLedger(
            code = code,
            title = title,
            notes = notes,
            sequence = sequence,
            discount = discount,
            region = region,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a utilization ledger in status created. */
fun newUtilizationLedger(code: String, title: String): UtilizationLedger {
    return UtilizationLedger(
        code = code,
        title = title,
        notes = "",
        sequence = 0,
        discount = 0.0,
        region = "",
        startsAt = 0,
        status = UtilizationLedgerStatus.CREATED,
    )
}
