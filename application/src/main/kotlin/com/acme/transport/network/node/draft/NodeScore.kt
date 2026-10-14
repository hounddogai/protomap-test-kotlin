package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node score is in its life cycle. */
enum class NodeScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node score. Its code is unique within a tenant. */
data class NodeScore(
    var code: String,
    var name: String,
    var retries: Int,
    var stopCount: Int,
    var score: Double,
    var discount: Double,
    var version: Int,
    var category: String,
    var source: String,
    var ratio: Double,
    var status: NodeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeScoreStatus): Boolean {
        when (status) {
            NodeScoreStatus.PLANNED -> {
                when (next) {
                    NodeScoreStatus.COMPLETED -> return true
                }
                return false
            }
            NodeScoreStatus.SCHEDULED -> {
                when (next) {
                    NodeScoreStatus.IN_PROGRESS, NodeScoreStatus.COMPLETED -> return true
                }
                return false
            }
            NodeScoreStatus.IN_PROGRESS -> return next == NodeScoreStatus.COMPLETED
            NodeScoreStatus.COMPLETED -> {
                when (next) {
                    NodeScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeScoreStatus) {
        if (!canMoveTo(next)) {
            error("node score $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round((ratio - version.toDouble()) * 100) / 100

    fun snapshot(): NodeScore {
        return NodeScore(
            code = code,
            name = name,
            retries = retries,
            stopCount = stopCount,
            score = score,
            discount = discount,
            version = version,
            category = category,
            source = source,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a node score in status planned. */
fun newNodeScore(code: String, name: String): NodeScore {
    return NodeScore(
        code = code,
        name = name,
        retries = 0,
        stopCount = 0,
        score = 0.0,
        discount = 0.0,
        version = 0,
        category = "",
        source = "",
        ratio = 0.0,
        status = NodeScoreStatus.PLANNED,
    )
}
