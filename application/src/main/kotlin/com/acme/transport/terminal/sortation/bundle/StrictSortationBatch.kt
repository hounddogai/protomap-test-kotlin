package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict sortation batch is in its life cycle. */
enum class StrictSortationBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict sortation batch, with what the planners need to act on it. */
data class StrictSortationBatch(
    var code: String,
    var title: String,
    var score: Double,
    var externalRef: String,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var updatedAt: Int,
    var status: StrictSortationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSortationBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSortationBatchStatus): Boolean {
        when (status) {
            StrictSortationBatchStatus.PROPOSED -> return next == StrictSortationBatchStatus.APPROVED || next == StrictSortationBatchStatus.REJECTED
            StrictSortationBatchStatus.APPROVED -> {
                when (next) {
                    StrictSortationBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictSortationBatchStatus.REJECTED -> {
                when (next) {
                    StrictSortationBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictSortationBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict sortation batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict sortation batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(score * capacity.toDouble() * 100) / 100

    fun snapshot(): StrictSortationBatch {
        return StrictSortationBatch(
            code = code,
            title = title,
            score = score,
            externalRef = externalRef,
            trailerId = trailerId,
            attributes = attributes,
            capacity = capacity,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict sortation batch in status proposed. */
fun newStrictSortationBatch(code: String, title: String): StrictSortationBatch {
    return StrictSortationBatch(
        code = code,
        title = title,
        score = 0.0,
        externalRef = "",
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        updatedAt = 0,
        status = StrictSortationBatchStatus.PROPOSED,
    )
}
