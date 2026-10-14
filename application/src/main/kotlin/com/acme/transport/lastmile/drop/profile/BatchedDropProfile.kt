package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.text.normalize

// The batched drop profile records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a batched drop profile is in its life cycle. */
enum class BatchedDropProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a batched drop profile and its current status. */
data class BatchedDropProfile(
    var code: String,
    var label: String,
    var description: String,
    var pinned: Boolean,
    var archived: Boolean,
    var score: Double,
    var status: BatchedDropProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDropProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDropProfileStatus): Boolean {
        when (status) {
            BatchedDropProfileStatus.CREATED -> return next == BatchedDropProfileStatus.PUBLISHED
            BatchedDropProfileStatus.VALIDATED -> return next == BatchedDropProfileStatus.PUBLISHED || next == BatchedDropProfileStatus.RETIRED
            BatchedDropProfileStatus.PUBLISHED -> {
                when (next) {
                    BatchedDropProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDropProfileStatus) {
        if (!canMoveTo(next)) {
            error("batched drop profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = score

    fun snapshot(): BatchedDropProfile {
        return BatchedDropProfile(
            code = code,
            label = label,
            description = description,
            pinned = pinned,
            archived = archived,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched drop profile in status created. */
fun newBatchedDropProfile(code: String, label: String): BatchedDropProfile {
    return BatchedDropProfile(
        code = code,
        label = label,
        description = "",
        pinned = false,
        archived = false,
        score = 0.0,
        status = BatchedDropProfileStatus.CREATED,
    )
}
