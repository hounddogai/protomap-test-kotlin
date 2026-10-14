package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize

/** Where a pending cutoff review is in its life cycle. */
enum class PendingCutoffReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending cutoff review, with what the planners need to act on it. */
data class PendingCutoffReview(
    var code: String,
    var label: String,
    var channel: String,
    var visible: Boolean,
    var attempts: Int,
    var carrier: String,
    var status: PendingCutoffReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCutoffReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCutoffReviewStatus): Boolean {
        when (status) {
            PendingCutoffReviewStatus.NEW -> {
                when (next) {
                    PendingCutoffReviewStatus.RUNNING -> return true
                }
                return false
            }
            PendingCutoffReviewStatus.QUEUED -> {
                when (next) {
                    PendingCutoffReviewStatus.DONE -> return true
                }
                return false
            }
            PendingCutoffReviewStatus.RUNNING -> return next == PendingCutoffReviewStatus.DONE || next == PendingCutoffReviewStatus.FAILED
            PendingCutoffReviewStatus.DONE -> {
                when (next) {
                    PendingCutoffReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCutoffReviewStatus) {
        if (!canMoveTo(next)) {
            error("pending cutoff review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): PendingCutoffReview {
        return PendingCutoffReview(
            code = code,
            label = label,
            channel = channel,
            visible = visible,
            attempts = attempts,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending cutoff review in status new. */
fun newPendingCutoffReview(code: String, label: String): PendingCutoffReview {
    return PendingCutoffReview(
        code = code,
        label = label,
        channel = "",
        visible = false,
        attempts = 0,
        carrier = "",
        status = PendingCutoffReviewStatus.NEW,
    )
}
