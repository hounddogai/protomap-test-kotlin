package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial metric records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial metric is in its life cycle. */
enum class AccessorialMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One accessorial metric, with what the planners need to act on it. */
data class AccessorialMetric(
    var code: String,
    var label: String,
    var dock: String,
    var phone: String,
    var revision: Int,
    var limit: Int,
    var source: String,
    var pinned: Boolean,
    var minimum: Double,
    var status: AccessorialMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialMetricStatus): Boolean {
        when (status) {
            AccessorialMetricStatus.PLANNED -> return next == AccessorialMetricStatus.COMPLETED
            AccessorialMetricStatus.SCHEDULED -> {
                when (next) {
                    AccessorialMetricStatus.COMPLETED -> return true
                }
                return false
            }
            AccessorialMetricStatus.IN_PROGRESS -> return next == AccessorialMetricStatus.CANCELLED
            AccessorialMetricStatus.COMPLETED -> return next == AccessorialMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AccessorialMetricStatus) {
        if (!canMoveTo(next)) {
            error("accessorial metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"accessorial metric"} $code"

    fun weightedLoad(): Double = round((minimum - revision.toDouble()) * 100) / 100

    fun snapshot(): AccessorialMetric {
        return AccessorialMetric(
            code = code,
            label = label,
            dock = dock,
            phone = phone,
            revision = revision,
            limit = limit,
            source = source,
            pinned = pinned,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a accessorial metric in status planned. */
fun newAccessorialMetric(code: String, label: String): AccessorialMetric {
    return AccessorialMetric(
        code = code,
        label = label,
        dock = "",
        phone = "",
        revision = 0,
        limit = 0,
        source = "",
        pinned = false,
        minimum = 0.0,
        status = AccessorialMetricStatus.PLANNED,
    )
}
