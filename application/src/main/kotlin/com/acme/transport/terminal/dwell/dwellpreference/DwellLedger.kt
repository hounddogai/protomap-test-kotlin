package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell ledger is in its life cycle. */
enum class DwellLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dwell ledger and its current status. */
data class DwellLedger(
    var code: String,
    var name: String,
    var country: String,
    var revision: Int,
    var version: Int,
    var status: DwellLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellLedgerStatus): Boolean {
        when (status) {
            DwellLedgerStatus.DRAFT -> return next == DwellLedgerStatus.PENDING || next == DwellLedgerStatus.SUSPENDED
            DwellLedgerStatus.PENDING -> return next == DwellLedgerStatus.ACTIVE || next == DwellLedgerStatus.CLOSED
            DwellLedgerStatus.ACTIVE -> return next == DwellLedgerStatus.SUSPENDED || next == DwellLedgerStatus.CLOSED
            DwellLedgerStatus.SUSPENDED -> return next == DwellLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DwellLedgerStatus) {
        if (!canMoveTo(next)) {
            error("dwell ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((version.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): DwellLedger {
        return DwellLedger(
            code = code,
            name = name,
            country = country,
            revision = revision,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dwell ledger in status draft. */
fun newDwellLedger(code: String, name: String): DwellLedger {
    return DwellLedger(
        code = code,
        name = name,
        country = "",
        revision = 0,
        version = 0,
        status = DwellLedgerStatus.DRAFT,
    )
}
