package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize

/** Where a regional zone ledger is in its life cycle. */
enum class RegionalZoneLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional zone ledger and its current status. */
data class RegionalZoneLedger(
    var code: String,
    var title: String,
    var driverId: String,
    var approved: Boolean,
    var label: String,
    var description: String,
    var country: String,
    var quantity: Int,
    var status: RegionalZoneLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalZoneLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalZoneLedgerStatus): Boolean {
        when (status) {
            RegionalZoneLedgerStatus.PROPOSED -> return next == RegionalZoneLedgerStatus.APPROVED
            RegionalZoneLedgerStatus.APPROVED -> {
                when (next) {
                    RegionalZoneLedgerStatus.REJECTED -> return true
                }
                return false
            }
            RegionalZoneLedgerStatus.REJECTED -> return next == RegionalZoneLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalZoneLedgerStatus) {
        if (!canMoveTo(next)) {
            error("regional zone ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RegionalZoneLedger {
        return RegionalZoneLedger(
            code = code,
            title = title,
            driverId = driverId,
            approved = approved,
            label = label,
            description = description,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional zone ledger in status proposed. */
fun newRegionalZoneLedger(code: String, title: String): RegionalZoneLedger {
    return RegionalZoneLedger(
        code = code,
        title = title,
        driverId = "",
        approved = false,
        label = "",
        description = "",
        country = "",
        quantity = 0,
        status = RegionalZoneLedgerStatus.PROPOSED,
    )
}
