package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.text.normalize

/** Where a adaptive broker metric is in its life cycle. */
enum class AdaptiveBrokerMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive broker metric, with what the planners need to act on it. */
data class AdaptiveBrokerMetric(
    var code: String,
    var name: String,
    var lane: String,
    var summary: String,
    var label: String,
    var quantity: Int,
    var status: AdaptiveBrokerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBrokerMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBrokerMetricStatus): Boolean {
        when (status) {
            AdaptiveBrokerMetricStatus.REQUESTED -> {
                when (next) {
                    AdaptiveBrokerMetricStatus.GRANTED, AdaptiveBrokerMetricStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveBrokerMetricStatus.GRANTED -> {
                when (next) {
                    AdaptiveBrokerMetricStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveBrokerMetricStatus.REVOKED -> {
                when (next) {
                    AdaptiveBrokerMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBrokerMetricStatus) {
        if (!canMoveTo(next)) {
            error("adaptive broker metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AdaptiveBrokerMetric {
        return AdaptiveBrokerMetric(
            code = code,
            name = name,
            lane = lane,
            summary = summary,
            label = label,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive broker metric in status requested. */
fun newAdaptiveBrokerMetric(code: String, name: String): AdaptiveBrokerMetric {
    return AdaptiveBrokerMetric(
        code = code,
        name = name,
        lane = "",
        summary = "",
        label = "",
        quantity = 0,
        status = AdaptiveBrokerMetricStatus.REQUESTED,
    )
}
