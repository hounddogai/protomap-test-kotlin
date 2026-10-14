package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker metric is in its life cycle. */
enum class BrokerMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One broker metric, with what the planners need to act on it. */
data class BrokerMetric(
    var code: String,
    var label: String,
    var sequence: Int,
    var createdAt: Int,
    var segment: String,
    var ceiling: Double,
    var tags: MutableList<String>,
    var threshold: Double,
    var shiftCode: String,
    var score: Double,
    var status: BrokerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerMetricStatus): Boolean {
        when (status) {
            BrokerMetricStatus.PROPOSED -> return next == BrokerMetricStatus.APPROVED || next == BrokerMetricStatus.WITHDRAWN
            BrokerMetricStatus.APPROVED -> return next == BrokerMetricStatus.WITHDRAWN
            BrokerMetricStatus.REJECTED -> return next == BrokerMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BrokerMetricStatus) {
        if (!canMoveTo(next)) {
            error("broker metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"broker metric"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((threshold - ceiling) * 100) / 100

    fun snapshot(): BrokerMetric {
        return BrokerMetric(
            code = code,
            label = label,
            sequence = sequence,
            createdAt = createdAt,
            segment = segment,
            ceiling = ceiling,
            tags = tags,
            threshold = threshold,
            shiftCode = shiftCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a broker metric in status proposed. */
fun newBrokerMetric(code: String, label: String): BrokerMetric {
    return BrokerMetric(
        code = code,
        label = label,
        sequence = 0,
        createdAt = 0,
        segment = "",
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        shiftCode = "",
        score = 0.0,
        status = BrokerMetricStatus.PROPOSED,
    )
}
