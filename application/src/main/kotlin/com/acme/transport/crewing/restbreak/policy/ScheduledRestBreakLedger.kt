package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled rest break ledger is in its life cycle. */
enum class ScheduledRestBreakLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled rest break ledger as the rest break team keeps it. */
data class ScheduledRestBreakLedger(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var revision: Int,
    var shiftCode: String,
    var contactEmail: String,
    var name: String,
    var ratio: Double,
    var archived: Boolean,
    var status: ScheduledRestBreakLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakLedgerStatus): Boolean {
        when (status) {
            ScheduledRestBreakLedgerStatus.OPEN -> {
                when (next) {
                    ScheduledRestBreakLedgerStatus.IN_REVIEW, ScheduledRestBreakLedgerStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledRestBreakLedgerStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledRestBreakLedgerStatus.REOPENED, ScheduledRestBreakLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledRestBreakLedgerStatus.RESOLVED -> return next == ScheduledRestBreakLedgerStatus.REOPENED || next == ScheduledRestBreakLedgerStatus.ARCHIVED
            ScheduledRestBreakLedgerStatus.REOPENED -> return next == ScheduledRestBreakLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakLedgerStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((ratio - revision.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRestBreakLedger {
        return ScheduledRestBreakLedger(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            revision = revision,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            name = name,
            ratio = ratio,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled rest break ledger in status open. */
fun newScheduledRestBreakLedger(code: String, label: String): ScheduledRestBreakLedger {
    return ScheduledRestBreakLedger(
        code = code,
        label = label,
        durationMinutes = 0,
        revision = 0,
        shiftCode = "",
        contactEmail = "",
        name = "",
        ratio = 0.0,
        archived = false,
        status = ScheduledRestBreakLedgerStatus.OPEN,
    )
}
