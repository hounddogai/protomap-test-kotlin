package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dispute ledger is in its life cycle. */
enum class PrimaryDisputeLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary dispute ledger as the dispute team keeps it. */
data class PrimaryDisputeLedger(
    var code: String,
    var label: String,
    var ceiling: Double,
    var segment: String,
    var active: Boolean,
    var capacity: Int,
    var description: String,
    var amount: Double,
    var vehicleId: String,
    var contactEmail: String,
    var source: String,
    var status: PrimaryDisputeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDisputeLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDisputeLedgerStatus): Boolean {
        when (status) {
            PrimaryDisputeLedgerStatus.CREATED -> return next == PrimaryDisputeLedgerStatus.VALIDATED || next == PrimaryDisputeLedgerStatus.PUBLISHED
            PrimaryDisputeLedgerStatus.VALIDATED -> {
                when (next) {
                    PrimaryDisputeLedgerStatus.PUBLISHED, PrimaryDisputeLedgerStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryDisputeLedgerStatus.PUBLISHED -> {
                when (next) {
                    PrimaryDisputeLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDisputeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary dispute ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary dispute ledger"} $code"

    fun netValue(): Double = round((ceiling + amount) * 100) / 100

    fun snapshot(): PrimaryDisputeLedger {
        return PrimaryDisputeLedger(
            code = code,
            label = label,
            ceiling = ceiling,
            segment = segment,
            active = active,
            capacity = capacity,
            description = description,
            amount = amount,
            vehicleId = vehicleId,
            contactEmail = contactEmail,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary dispute ledger in status created. */
fun newPrimaryDisputeLedger(code: String, label: String): PrimaryDisputeLedger {
    return PrimaryDisputeLedger(
        code = code,
        label = label,
        ceiling = 0.0,
        segment = "",
        active = false,
        capacity = 0,
        description = "",
        amount = 0.0,
        vehicleId = "",
        contactEmail = "",
        source = "",
        status = PrimaryDisputeLedgerStatus.CREATED,
    )
}
