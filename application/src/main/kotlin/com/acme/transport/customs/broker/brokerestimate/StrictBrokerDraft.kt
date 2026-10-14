package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.text.normalize

/** Where a strict broker draft is in its life cycle. */
enum class StrictBrokerDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict broker draft. Its code is unique within a tenant. */
data class StrictBrokerDraft(
    var code: String,
    var label: String,
    var ownerId: String,
    var timezone: String,
    var summary: String,
    var channel: String,
    var amount: Double,
    var status: StrictBrokerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBrokerDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBrokerDraftStatus): Boolean {
        when (status) {
            StrictBrokerDraftStatus.DRAFT -> return next == StrictBrokerDraftStatus.SUSPENDED || next == StrictBrokerDraftStatus.CLOSED
            StrictBrokerDraftStatus.PENDING -> return next == StrictBrokerDraftStatus.ACTIVE
            StrictBrokerDraftStatus.ACTIVE -> return next == StrictBrokerDraftStatus.CLOSED
            StrictBrokerDraftStatus.SUSPENDED -> return next == StrictBrokerDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictBrokerDraftStatus) {
        if (!canMoveTo(next)) {
            error("strict broker draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = amount

    fun snapshot(): StrictBrokerDraft {
        return StrictBrokerDraft(
            code = code,
            label = label,
            ownerId = ownerId,
            timezone = timezone,
            summary = summary,
            channel = channel,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict broker draft in status draft. */
fun newStrictBrokerDraft(code: String, label: String): StrictBrokerDraft {
    return StrictBrokerDraft(
        code = code,
        label = label,
        ownerId = "",
        timezone = "",
        summary = "",
        channel = "",
        amount = 0.0,
        status = StrictBrokerDraftStatus.DRAFT,
    )
}
