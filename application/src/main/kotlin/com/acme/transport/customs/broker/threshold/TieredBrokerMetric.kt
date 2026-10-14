package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered broker metric is in its life cycle. */
enum class TieredBrokerMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered broker metric. Its code is unique within a tenant. */
data class TieredBrokerMetric(
    var code: String,
    var title: String,
    var source: String,
    var threshold: Double,
    var endsAt: Int,
    var score: Double,
    var status: TieredBrokerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBrokerMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBrokerMetricStatus): Boolean {
        when (status) {
            TieredBrokerMetricStatus.PROPOSED -> return next == TieredBrokerMetricStatus.REJECTED
            TieredBrokerMetricStatus.APPROVED -> {
                when (next) {
                    TieredBrokerMetricStatus.REJECTED, TieredBrokerMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredBrokerMetricStatus.REJECTED -> return next == TieredBrokerMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredBrokerMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered broker metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round((score + endsAt.toDouble()) * 100) / 100

    fun snapshot(): TieredBrokerMetric {
        return TieredBrokerMetric(
            code = code,
            title = title,
            source = source,
            threshold = threshold,
            endsAt = endsAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered broker metric in status proposed. */
fun newTieredBrokerMetric(code: String, title: String): TieredBrokerMetric {
    return TieredBrokerMetric(
        code = code,
        title = title,
        source = "",
        threshold = 0.0,
        endsAt = 0,
        score = 0.0,
        status = TieredBrokerMetricStatus.PROPOSED,
    )
}
