package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time metric is in its life cycle. */
enum class TransitTimeMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A transit time metric as the transit time team keeps it. */
data class TransitTimeMetric(
    var code: String,
    var title: String,
    var threshold: Double,
    var driverId: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var priority: Int,
    var version: Int,
    var region: String,
    var status: TransitTimeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeMetricStatus): Boolean {
        when (status) {
            TransitTimeMetricStatus.DRAFT -> {
                when (next) {
                    TransitTimeMetricStatus.SUSPENDED, TransitTimeMetricStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimeMetricStatus.PENDING -> {
                when (next) {
                    TransitTimeMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            TransitTimeMetricStatus.ACTIVE -> {
                when (next) {
                    TransitTimeMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            TransitTimeMetricStatus.SUSPENDED -> {
                when (next) {
                    TransitTimeMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeMetricStatus) {
        if (!canMoveTo(next)) {
            error("transit time metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((version.toDouble() - threshold) * 100) / 100

    fun snapshot(): TransitTimeMetric {
        return TransitTimeMetric(
            code = code,
            title = title,
            threshold = threshold,
            driverId = driverId,
            archived = archived,
            attributes = attributes,
            priority = priority,
            version = version,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a transit time metric in status draft. */
fun newTransitTimeMetric(code: String, title: String): TransitTimeMetric {
    return TransitTimeMetric(
        code = code,
        title = title,
        threshold = 0.0,
        driverId = "",
        archived = false,
        attributes = mutableMapOf<String, String>(),
        priority = 0,
        version = 0,
        region = "",
        status = TransitTimeMetricStatus.DRAFT,
    )
}
