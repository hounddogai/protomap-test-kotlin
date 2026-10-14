package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.normalize

/** Where a batched node baseline is in its life cycle. */
enum class BatchedNodeBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched node baseline, with what the planners need to act on it. */
data class BatchedNodeBaseline(
    var code: String,
    var title: String,
    var threshold: Double,
    var tags: MutableList<String>,
    var contactEmail: String,
    var trailerId: String,
    var status: BatchedNodeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeBaselineStatus): Boolean {
        when (status) {
            BatchedNodeBaselineStatus.NEW -> {
                when (next) {
                    BatchedNodeBaselineStatus.RUNNING, BatchedNodeBaselineStatus.FAILED -> return true
                }
                return false
            }
            BatchedNodeBaselineStatus.QUEUED -> {
                when (next) {
                    BatchedNodeBaselineStatus.RUNNING, BatchedNodeBaselineStatus.DONE -> return true
                }
                return false
            }
            BatchedNodeBaselineStatus.RUNNING -> {
                when (next) {
                    BatchedNodeBaselineStatus.DONE -> return true
                }
                return false
            }
            BatchedNodeBaselineStatus.DONE -> return next == BatchedNodeBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedNodeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("batched node baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = threshold

    fun snapshot(): BatchedNodeBaseline {
        return BatchedNodeBaseline(
            code = code,
            title = title,
            threshold = threshold,
            tags = tags,
            contactEmail = contactEmail,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched node baseline in status new. */
fun newBatchedNodeBaseline(code: String, title: String): BatchedNodeBaseline {
    return BatchedNodeBaseline(
        code = code,
        title = title,
        threshold = 0.0,
        tags = mutableListOf<String>(),
        contactEmail = "",
        trailerId = "",
        status = BatchedNodeBaselineStatus.NEW,
    )
}
