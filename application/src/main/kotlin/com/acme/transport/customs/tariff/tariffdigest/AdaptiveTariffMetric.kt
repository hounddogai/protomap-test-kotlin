package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive tariff metric is in its life cycle. */
enum class AdaptiveTariffMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive tariff metric, with what the planners need to act on it. */
data class AdaptiveTariffMetric(
    var code: String,
    var name: String,
    var limit: Int,
    var stopCount: Int,
    var title: String,
    var quantity: Int,
    var routeCode: String,
    var status: AdaptiveTariffMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTariffMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTariffMetricStatus): Boolean {
        when (status) {
            AdaptiveTariffMetricStatus.REQUESTED -> {
                when (next) {
                    AdaptiveTariffMetricStatus.GRANTED -> return true
                }
                return false
            }
            AdaptiveTariffMetricStatus.GRANTED -> {
                when (next) {
                    AdaptiveTariffMetricStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveTariffMetricStatus.REVOKED -> return next == AdaptiveTariffMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveTariffMetricStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tariff metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive tariff metric"} $code"

    fun projectedValue(): Double = round((stopCount.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveTariffMetric {
        return AdaptiveTariffMetric(
            code = code,
            name = name,
            limit = limit,
            stopCount = stopCount,
            title = title,
            quantity = quantity,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive tariff metric in status requested. */
fun newAdaptiveTariffMetric(code: String, name: String): AdaptiveTariffMetric {
    return AdaptiveTariffMetric(
        code = code,
        name = name,
        limit = 0,
        stopCount = 0,
        title = "",
        quantity = 0,
        routeCode = "",
        status = AdaptiveTariffMetricStatus.REQUESTED,
    )
}
