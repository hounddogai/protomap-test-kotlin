package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional backhaul ledger is in its life cycle. */
enum class RegionalBackhaulLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional backhaul ledger. Its code is unique within a tenant. */
data class RegionalBackhaulLedger(
    var code: String,
    var name: String,
    var routeCode: String,
    var segment: String,
    var rate: Double,
    var category: String,
    var checksum: String,
    var summary: String,
    var description: String,
    var sequence: Int,
    var status: RegionalBackhaulLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBackhaulLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBackhaulLedgerStatus): Boolean {
        when (status) {
            RegionalBackhaulLedgerStatus.OPEN -> return next == RegionalBackhaulLedgerStatus.RESOLVED || next == RegionalBackhaulLedgerStatus.REOPENED
            RegionalBackhaulLedgerStatus.IN_REVIEW -> return next == RegionalBackhaulLedgerStatus.RESOLVED || next == RegionalBackhaulLedgerStatus.REOPENED
            RegionalBackhaulLedgerStatus.RESOLVED -> {
                when (next) {
                    RegionalBackhaulLedgerStatus.REOPENED, RegionalBackhaulLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalBackhaulLedgerStatus.REOPENED -> return next == RegionalBackhaulLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalBackhaulLedgerStatus) {
        if (!canMoveTo(next)) {
            error("regional backhaul ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round(sequence.toDouble() * rate * 100) / 100

    fun snapshot(): RegionalBackhaulLedger {
        return RegionalBackhaulLedger(
            code = code,
            name = name,
            routeCode = routeCode,
            segment = segment,
            rate = rate,
            category = category,
            checksum = checksum,
            summary = summary,
            description = description,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional backhaul ledger in status open. */
fun newRegionalBackhaulLedger(code: String, name: String): RegionalBackhaulLedger {
    return RegionalBackhaulLedger(
        code = code,
        name = name,
        routeCode = "",
        segment = "",
        rate = 0.0,
        category = "",
        checksum = "",
        summary = "",
        description = "",
        sequence = 0,
        status = RegionalBackhaulLedgerStatus.OPEN,
    )
}
