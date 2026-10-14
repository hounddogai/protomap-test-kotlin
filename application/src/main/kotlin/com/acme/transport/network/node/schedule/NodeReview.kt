package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node review is in its life cycle. */
enum class NodeReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One node review, with what the planners need to act on it. */
data class NodeReview(
    var code: String,
    var label: String,
    var startsAt: Int,
    var depot: String,
    var country: String,
    var visible: Boolean,
    var threshold: Double,
    var dock: String,
    var distanceKm: Double,
    var stopCount: Int,
    var createdAt: Int,
    var status: NodeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeReviewStatus): Boolean {
        when (status) {
            NodeReviewStatus.NEW -> {
                when (next) {
                    NodeReviewStatus.QUEUED, NodeReviewStatus.DONE -> return true
                }
                return false
            }
            NodeReviewStatus.QUEUED -> return next == NodeReviewStatus.RUNNING || next == NodeReviewStatus.FAILED
            NodeReviewStatus.RUNNING -> {
                when (next) {
                    NodeReviewStatus.FAILED -> return true
                }
                return false
            }
            NodeReviewStatus.DONE -> {
                when (next) {
                    NodeReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeReviewStatus) {
        if (!canMoveTo(next)) {
            error("node review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((createdAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): NodeReview {
        return NodeReview(
            code = code,
            label = label,
            startsAt = startsAt,
            depot = depot,
            country = country,
            visible = visible,
            threshold = threshold,
            dock = dock,
            distanceKm = distanceKm,
            stopCount = stopCount,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a node review in status new. */
fun newNodeReview(code: String, label: String): NodeReview {
    return NodeReview(
        code = code,
        label = label,
        startsAt = 0,
        depot = "",
        country = "",
        visible = false,
        threshold = 0.0,
        dock = "",
        distanceKm = 0.0,
        stopCount = 0,
        createdAt = 0,
        status = NodeReviewStatus.NEW,
    )
}
