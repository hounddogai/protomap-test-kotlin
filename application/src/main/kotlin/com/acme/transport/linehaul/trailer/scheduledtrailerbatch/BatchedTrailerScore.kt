package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched trailer score is in its life cycle. */
enum class BatchedTrailerScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched trailer score, with what the planners need to act on it. */
data class BatchedTrailerScore(
    var code: String,
    var label: String,
    var cost: Double,
    var revision: Int,
    var attributes: MutableMap<String, String>,
    var windowDays: Int,
    var status: BatchedTrailerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerScoreStatus): Boolean {
        when (status) {
            BatchedTrailerScoreStatus.IDLE -> return next == BatchedTrailerScoreStatus.WARMING || next == BatchedTrailerScoreStatus.DRAINING
            BatchedTrailerScoreStatus.WARMING -> {
                when (next) {
                    BatchedTrailerScoreStatus.DRAINING -> return true
                }
                return false
            }
            BatchedTrailerScoreStatus.READY -> {
                when (next) {
                    BatchedTrailerScoreStatus.DRAINING, BatchedTrailerScoreStatus.STOPPED -> return true
                }
                return false
            }
            BatchedTrailerScoreStatus.DRAINING -> {
                when (next) {
                    BatchedTrailerScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerScoreStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer score $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(revision.toDouble() * cost * 100) / 100

    fun snapshot(): BatchedTrailerScore {
        return BatchedTrailerScore(
            code = code,
            label = label,
            cost = cost,
            revision = revision,
            attributes = attributes,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched trailer score in status idle. */
fun newBatchedTrailerScore(code: String, label: String): BatchedTrailerScore {
    return BatchedTrailerScore(
        code = code,
        label = label,
        cost = 0.0,
        revision = 0,
        attributes = mutableMapOf<String, String>(),
        windowDays = 0,
        status = BatchedTrailerScoreStatus.IDLE,
    )
}
