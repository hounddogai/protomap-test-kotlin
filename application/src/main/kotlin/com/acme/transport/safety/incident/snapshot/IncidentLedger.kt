package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.normalize

/** Where a incident ledger is in its life cycle. */
enum class IncidentLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a incident ledger and its current status. */
data class IncidentLedger(
    var code: String,
    var title: String,
    var timezone: String,
    var visible: Boolean,
    var threshold: Double,
    var currency: String,
    var status: IncidentLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentLedgerStatus): Boolean {
        when (status) {
            IncidentLedgerStatus.CREATED -> return next == IncidentLedgerStatus.VALIDATED || next == IncidentLedgerStatus.PUBLISHED
            IncidentLedgerStatus.VALIDATED -> return next == IncidentLedgerStatus.PUBLISHED
            IncidentLedgerStatus.PUBLISHED -> {
                when (next) {
                    IncidentLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentLedgerStatus) {
        if (!canMoveTo(next)) {
            error("incident ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = threshold

    fun snapshot(): IncidentLedger {
        return IncidentLedger(
            code = code,
            title = title,
            timezone = timezone,
            visible = visible,
            threshold = threshold,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident ledger in status created. */
fun newIncidentLedger(code: String, title: String): IncidentLedger {
    return IncidentLedger(
        code = code,
        title = title,
        timezone = "",
        visible = false,
        threshold = 0.0,
        currency = "",
        status = IncidentLedgerStatus.CREATED,
    )
}
