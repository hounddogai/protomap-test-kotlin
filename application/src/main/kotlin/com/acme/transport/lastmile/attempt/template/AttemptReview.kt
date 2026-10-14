package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt review is in its life cycle. */
enum class AttemptReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt review. Its code is unique within a tenant. */
data class AttemptReview(
    var code: String,
    var name: String,
    var ceiling: Double,
    var priority: Int,
    var shiftCode: String,
    var notes: String,
    var status: AttemptReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptReviewStatus): Boolean {
        when (status) {
            AttemptReviewStatus.NEW -> return next == AttemptReviewStatus.RUNNING || next == AttemptReviewStatus.FAILED
            AttemptReviewStatus.QUEUED -> {
                when (next) {
                    AttemptReviewStatus.RUNNING, AttemptReviewStatus.DONE -> return true
                }
                return false
            }
            AttemptReviewStatus.RUNNING -> return next == AttemptReviewStatus.DONE || next == AttemptReviewStatus.FAILED
            AttemptReviewStatus.DONE -> return next == AttemptReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AttemptReviewStatus) {
        if (!canMoveTo(next)) {
            error("attempt review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round(priority.toDouble() * ceiling * 100) / 100

    fun snapshot(): AttemptReview {
        return AttemptReview(
            code = code,
            name = name,
            ceiling = ceiling,
            priority = priority,
            shiftCode = shiftCode,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a attempt review in status new. */
fun newAttemptReview(code: String, name: String): AttemptReview {
    return AttemptReview(
        code = code,
        name = name,
        ceiling = 0.0,
        priority = 0,
        shiftCode = "",
        notes = "",
        status = AttemptReviewStatus.NEW,
    )
}
