package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.text.normalize

/** Where a scheduled redelivery entry is in its life cycle. */
enum class ScheduledRedeliveryEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled redelivery entry. Its code is unique within a tenant. */
data class ScheduledRedeliveryEntry(
    var code: String,
    var label: String,
    var reviewer: String,
    var score: Double,
    var ownerId: String,
    var status: ScheduledRedeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryEntryStatus): Boolean {
        when (status) {
            ScheduledRedeliveryEntryStatus.OPEN -> {
                when (next) {
                    ScheduledRedeliveryEntryStatus.IN_REVIEW, ScheduledRedeliveryEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledRedeliveryEntryStatus.IN_REVIEW -> return next == ScheduledRedeliveryEntryStatus.RESOLVED || next == ScheduledRedeliveryEntryStatus.ARCHIVED
            ScheduledRedeliveryEntryStatus.RESOLVED -> {
                when (next) {
                    ScheduledRedeliveryEntryStatus.REOPENED, ScheduledRedeliveryEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledRedeliveryEntryStatus.REOPENED -> {
                when (next) {
                    ScheduledRedeliveryEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled redelivery entry"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): ScheduledRedeliveryEntry {
        return ScheduledRedeliveryEntry(
            code = code,
            label = label,
            reviewer = reviewer,
            score = score,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled redelivery entry in status open. */
fun newScheduledRedeliveryEntry(code: String, label: String): ScheduledRedeliveryEntry {
    return ScheduledRedeliveryEntry(
        code = code,
        label = label,
        reviewer = "",
        score = 0.0,
        ownerId = "",
        status = ScheduledRedeliveryEntryStatus.OPEN,
    )
}
