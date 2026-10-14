package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default idling metric is in its life cycle. */
enum class DefaultIdlingMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default idling metric and its current status. */
data class DefaultIdlingMetric(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var rate: Double,
    var carrier: String,
    var revision: Int,
    var limit: Int,
    var version: Int,
    var etaMinutes: Int,
    var quantity: Int,
    var capacity: Int,
    var status: DefaultIdlingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIdlingMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIdlingMetricStatus): Boolean {
        when (status) {
            DefaultIdlingMetricStatus.CREATED -> return next == DefaultIdlingMetricStatus.PUBLISHED
            DefaultIdlingMetricStatus.VALIDATED -> {
                when (next) {
                    DefaultIdlingMetricStatus.RETIRED -> return true
                }
                return false
            }
            DefaultIdlingMetricStatus.PUBLISHED -> {
                when (next) {
                    DefaultIdlingMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultIdlingMetricStatus) {
        if (!canMoveTo(next)) {
            error("default idling metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(rate * version.toDouble() * 100) / 100

    fun snapshot(): DefaultIdlingMetric {
        return DefaultIdlingMetric(
            code = code,
            title = title,
            distanceKm = distanceKm,
            rate = rate,
            carrier = carrier,
            revision = revision,
            limit = limit,
            version = version,
            etaMinutes = etaMinutes,
            quantity = quantity,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default idling metric in status created. */
fun newDefaultIdlingMetric(code: String, title: String): DefaultIdlingMetric {
    return DefaultIdlingMetric(
        code = code,
        title = title,
        distanceKm = 0.0,
        rate = 0.0,
        carrier = "",
        revision = 0,
        limit = 0,
        version = 0,
        etaMinutes = 0,
        quantity = 0,
        capacity = 0,
        status = DefaultIdlingMetricStatus.CREATED,
    )
}
