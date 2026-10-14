package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary coaching ledger is in its life cycle. */
enum class PrimaryCoachingLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary coaching ledger. Its code is unique within a tenant. */
data class PrimaryCoachingLedger(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var trailerId: String,
    var discount: Double,
    var label: String,
    var reviewer: String,
    var timezone: String,
    var status: PrimaryCoachingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCoachingLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCoachingLedgerStatus): Boolean {
        when (status) {
            PrimaryCoachingLedgerStatus.CREATED -> {
                when (next) {
                    PrimaryCoachingLedgerStatus.VALIDATED -> return true
                }
                return false
            }
            PrimaryCoachingLedgerStatus.VALIDATED -> return next == PrimaryCoachingLedgerStatus.PUBLISHED || next == PrimaryCoachingLedgerStatus.RETIRED
            PrimaryCoachingLedgerStatus.PUBLISHED -> return next == PrimaryCoachingLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryCoachingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary coaching ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary coaching ledger"} $code"

    fun projectedValue(): Double = round((fuelLiters + discount) * 100) / 100

    fun snapshot(): PrimaryCoachingLedger {
        return PrimaryCoachingLedger(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            discount = discount,
            label = label,
            reviewer = reviewer,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary coaching ledger in status created. */
fun newPrimaryCoachingLedger(code: String, title: String): PrimaryCoachingLedger {
    return PrimaryCoachingLedger(
        code = code,
        title = title,
        fuelLiters = 0.0,
        trailerId = "",
        discount = 0.0,
        label = "",
        reviewer = "",
        timezone = "",
        status = PrimaryCoachingLedgerStatus.CREATED,
    )
}
