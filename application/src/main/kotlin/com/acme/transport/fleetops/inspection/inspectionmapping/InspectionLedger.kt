package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.kit.text.normalize

/** Where a inspection ledger is in its life cycle. */
enum class InspectionLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a inspection ledger and its current status. */
data class InspectionLedger(
    var code: String,
    var title: String,
    var approved: Boolean,
    var tags: MutableList<String>,
    var minimum: Double,
    var status: InspectionLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionLedgerStatus): Boolean {
        when (status) {
            InspectionLedgerStatus.OPEN -> return next == InspectionLedgerStatus.RESOLVED
            InspectionLedgerStatus.IN_REVIEW -> {
                when (next) {
                    InspectionLedgerStatus.RESOLVED, InspectionLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            InspectionLedgerStatus.RESOLVED -> return next == InspectionLedgerStatus.REOPENED
            InspectionLedgerStatus.REOPENED -> {
                when (next) {
                    InspectionLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionLedgerStatus) {
        if (!canMoveTo(next)) {
            error("inspection ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = minimum

    fun snapshot(): InspectionLedger {
        return InspectionLedger(
            code = code,
            title = title,
            approved = approved,
            tags = tags,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection ledger in status open. */
fun newInspectionLedger(code: String, title: String): InspectionLedger {
    return InspectionLedger(
        code = code,
        title = title,
        approved = false,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        status = InspectionLedgerStatus.OPEN,
    )
}
