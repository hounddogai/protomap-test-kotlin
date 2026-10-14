package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.text.normalize

/** Where a scheduled sortation ledger is in its life cycle. */
enum class ScheduledSortationLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled sortation ledger and its current status. */
data class ScheduledSortationLedger(
    var code: String,
    var label: String,
    var phone: String,
    var approved: Boolean,
    var locale: String,
    var amount: Double,
    var status: ScheduledSortationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSortationLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSortationLedgerStatus): Boolean {
        when (status) {
            ScheduledSortationLedgerStatus.DRAFT -> {
                when (next) {
                    ScheduledSortationLedgerStatus.PENDING -> return true
                }
                return false
            }
            ScheduledSortationLedgerStatus.PENDING -> return next == ScheduledSortationLedgerStatus.ACTIVE || next == ScheduledSortationLedgerStatus.SUSPENDED
            ScheduledSortationLedgerStatus.ACTIVE -> {
                when (next) {
                    ScheduledSortationLedgerStatus.SUSPENDED, ScheduledSortationLedgerStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledSortationLedgerStatus.SUSPENDED -> {
                when (next) {
                    ScheduledSortationLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSortationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("scheduled sortation ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): ScheduledSortationLedger {
        return ScheduledSortationLedger(
            code = code,
            label = label,
            phone = phone,
            approved = approved,
            locale = locale,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled sortation ledger in status draft. */
fun newScheduledSortationLedger(code: String, label: String): ScheduledSortationLedger {
    return ScheduledSortationLedger(
        code = code,
        label = label,
        phone = "",
        approved = false,
        locale = "",
        amount = 0.0,
        status = ScheduledSortationLedgerStatus.DRAFT,
    )
}
