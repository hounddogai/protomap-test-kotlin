package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling sortation review is in its life cycle. */
enum class RollingSortationReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling sortation review as the sortation team keeps it. */
data class RollingSortationReview(
    var code: String,
    var label: String,
    var batchSize: Int,
    var loadKg: Double,
    var durationMinutes: Int,
    var retries: Int,
    var summary: String,
    var etaMinutes: Int,
    var version: Int,
    var status: RollingSortationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSortationReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSortationReviewStatus): Boolean {
        when (status) {
            RollingSortationReviewStatus.NEW -> {
                when (next) {
                    RollingSortationReviewStatus.QUEUED, RollingSortationReviewStatus.DONE -> return true
                }
                return false
            }
            RollingSortationReviewStatus.QUEUED -> {
                when (next) {
                    RollingSortationReviewStatus.FAILED -> return true
                }
                return false
            }
            RollingSortationReviewStatus.RUNNING -> {
                when (next) {
                    RollingSortationReviewStatus.DONE -> return true
                }
                return false
            }
            RollingSortationReviewStatus.DONE -> return next == RollingSortationReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingSortationReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling sortation review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round(batchSize.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): RollingSortationReview {
        return RollingSortationReview(
            code = code,
            label = label,
            batchSize = batchSize,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            retries = retries,
            summary = summary,
            etaMinutes = etaMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling sortation review in status new. */
fun newRollingSortationReview(code: String, label: String): RollingSortationReview {
    return RollingSortationReview(
        code = code,
        label = label,
        batchSize = 0,
        loadKg = 0.0,
        durationMinutes = 0,
        retries = 0,
        summary = "",
        etaMinutes = 0,
        version = 0,
        status = RollingSortationReviewStatus.NEW,
    )
}
