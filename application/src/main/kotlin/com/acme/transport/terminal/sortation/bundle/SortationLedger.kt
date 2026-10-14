package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation ledger is in its life cycle. */
enum class SortationLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A sortation ledger. Its code is unique within a tenant. */
data class SortationLedger(
    var code: String,
    var title: String,
    var approved: Boolean,
    var maximum: Double,
    var distanceKm: Double,
    var reviewer: String,
    var status: SortationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationLedgerStatus): Boolean {
        when (status) {
            SortationLedgerStatus.REQUESTED -> {
                when (next) {
                    SortationLedgerStatus.REVOKED, SortationLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            SortationLedgerStatus.GRANTED -> return next == SortationLedgerStatus.REVOKED || next == SortationLedgerStatus.EXPIRED
            SortationLedgerStatus.REVOKED -> {
                when (next) {
                    SortationLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("sortation ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"sortation ledger"} $code"

    fun adjustedValue(): Double = round((maximum + distanceKm) * 100) / 100

    fun snapshot(): SortationLedger {
        return SortationLedger(
            code = code,
            title = title,
            approved = approved,
            maximum = maximum,
            distanceKm = distanceKm,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a sortation ledger in status requested. */
fun newSortationLedger(code: String, title: String): SortationLedger {
    return SortationLedger(
        code = code,
        title = title,
        approved = false,
        maximum = 0.0,
        distanceKm = 0.0,
        reviewer = "",
        status = SortationLedgerStatus.REQUESTED,
    )
}
