package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize

/** Where a pending near miss batch is in its life cycle. */
enum class PendingNearMissBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending near miss batch and its current status. */
data class PendingNearMissBatch(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var rate: Double,
    var trailerId: String,
    var status: PendingNearMissBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNearMissBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNearMissBatchStatus): Boolean {
        when (status) {
            PendingNearMissBatchStatus.CREATED -> {
                when (next) {
                    PendingNearMissBatchStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingNearMissBatchStatus.VALIDATED -> return next == PendingNearMissBatchStatus.RETIRED
            PendingNearMissBatchStatus.PUBLISHED -> {
                when (next) {
                    PendingNearMissBatchStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNearMissBatchStatus) {
        if (!canMoveTo(next)) {
            error("pending near miss batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = rate

    fun snapshot(): PendingNearMissBatch {
        return PendingNearMissBatch(
            code = code,
            label = label,
            pinned = pinned,
            rate = rate,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending near miss batch in status created. */
fun newPendingNearMissBatch(code: String, label: String): PendingNearMissBatch {
    return PendingNearMissBatch(
        code = code,
        label = label,
        pinned = false,
        rate = 0.0,
        trailerId = "",
        status = PendingNearMissBatchStatus.CREATED,
    )
}
