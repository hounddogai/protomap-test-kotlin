package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.normalize

// The attempt batch records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt batch is in its life cycle. */
enum class AttemptBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a attempt batch and its current status. */
data class AttemptBatch(
    var code: String,
    var name: String,
    var country: String,
    var rate: Double,
    var driverId: String,
    var pinned: Boolean,
    var dock: String,
    var externalRef: String,
    var status: AttemptBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptBatchStatus): Boolean {
        when (status) {
            AttemptBatchStatus.OPEN -> {
                when (next) {
                    AttemptBatchStatus.IN_REVIEW, AttemptBatchStatus.RESOLVED -> return true
                }
                return false
            }
            AttemptBatchStatus.IN_REVIEW -> {
                when (next) {
                    AttemptBatchStatus.RESOLVED, AttemptBatchStatus.REOPENED -> return true
                }
                return false
            }
            AttemptBatchStatus.RESOLVED -> return next == AttemptBatchStatus.REOPENED || next == AttemptBatchStatus.ARCHIVED
            AttemptBatchStatus.REOPENED -> return next == AttemptBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AttemptBatchStatus) {
        if (!canMoveTo(next)) {
            error("attempt batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt batch"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): AttemptBatch {
        return AttemptBatch(
            code = code,
            name = name,
            country = country,
            rate = rate,
            driverId = driverId,
            pinned = pinned,
            dock = dock,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a attempt batch in status open. */
fun newAttemptBatch(code: String, name: String): AttemptBatch {
    return AttemptBatch(
        code = code,
        name = name,
        country = "",
        rate = 0.0,
        driverId = "",
        pinned = false,
        dock = "",
        externalRef = "",
        status = AttemptBatchStatus.OPEN,
    )
}
