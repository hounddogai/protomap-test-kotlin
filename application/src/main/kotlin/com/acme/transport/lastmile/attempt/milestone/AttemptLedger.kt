package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt ledger is in its life cycle. */
enum class AttemptLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A attempt ledger. Its code is unique within a tenant. */
data class AttemptLedger(
    var code: String,
    var name: String,
    var channel: String,
    var graceDays: Int,
    var revision: Int,
    var visible: Boolean,
    var segment: String,
    var status: AttemptLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptLedgerStatus): Boolean {
        when (status) {
            AttemptLedgerStatus.DRAFT -> {
                when (next) {
                    AttemptLedgerStatus.PENDING -> return true
                }
                return false
            }
            AttemptLedgerStatus.PENDING -> return next == AttemptLedgerStatus.SUSPENDED || next == AttemptLedgerStatus.CLOSED
            AttemptLedgerStatus.ACTIVE -> {
                when (next) {
                    AttemptLedgerStatus.CLOSED -> return true
                }
                return false
            }
            AttemptLedgerStatus.SUSPENDED -> {
                when (next) {
                    AttemptLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptLedgerStatus) {
        if (!canMoveTo(next)) {
            error("attempt ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"attempt ledger"} $code"

    fun netLoad(): Double = round((graceDays.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): AttemptLedger {
        return AttemptLedger(
            code = code,
            name = name,
            channel = channel,
            graceDays = graceDays,
            revision = revision,
            visible = visible,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a attempt ledger in status draft. */
fun newAttemptLedger(code: String, name: String): AttemptLedger {
    return AttemptLedger(
        code = code,
        name = name,
        channel = "",
        graceDays = 0,
        revision = 0,
        visible = false,
        segment = "",
        status = AttemptLedgerStatus.DRAFT,
    )
}
