package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node metric is in its life cycle. */
enum class NodeMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One node metric, with what the planners need to act on it. */
data class NodeMetric(
    var code: String,
    var name: String,
    var amount: Double,
    var expiresAt: Int,
    var contactEmail: String,
    var loadKg: Double,
    var routeCode: String,
    var title: String,
    var locale: String,
    var stopCount: Int,
    var version: Int,
    var status: NodeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeMetricStatus): Boolean {
        when (status) {
            NodeMetricStatus.PROPOSED -> {
                when (next) {
                    NodeMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            NodeMetricStatus.APPROVED -> {
                when (next) {
                    NodeMetricStatus.REJECTED, NodeMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            NodeMetricStatus.REJECTED -> return next == NodeMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NodeMetricStatus) {
        if (!canMoveTo(next)) {
            error("node metric $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"node metric"} $code"

    fun netValue(): Double = round(loadKg * stopCount.toDouble() * 100) / 100

    fun snapshot(): NodeMetric {
        return NodeMetric(
            code = code,
            name = name,
            amount = amount,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            loadKg = loadKg,
            routeCode = routeCode,
            title = title,
            locale = locale,
            stopCount = stopCount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a node metric in status proposed. */
fun newNodeMetric(code: String, name: String): NodeMetric {
    return NodeMetric(
        code = code,
        name = name,
        amount = 0.0,
        expiresAt = 0,
        contactEmail = "",
        loadKg = 0.0,
        routeCode = "",
        title = "",
        locale = "",
        stopCount = 0,
        version = 0,
        status = NodeMetricStatus.PROPOSED,
    )
}
