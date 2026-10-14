package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute metric is in its life cycle. */
enum class DisputeMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One dispute metric, with what the planners need to act on it. */
data class DisputeMetric(
    var code: String,
    var label: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var threshold: Double,
    var depot: String,
    var amount: Double,
    var locale: String,
    var routeCode: String,
    var status: DisputeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeMetricStatus): Boolean {
        when (status) {
            DisputeMetricStatus.REQUESTED -> {
                when (next) {
                    DisputeMetricStatus.REVOKED -> return true
                }
                return false
            }
            DisputeMetricStatus.GRANTED -> {
                when (next) {
                    DisputeMetricStatus.REVOKED, DisputeMetricStatus.EXPIRED -> return true
                }
                return false
            }
            DisputeMetricStatus.REVOKED -> {
                when (next) {
                    DisputeMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeMetricStatus) {
        if (!canMoveTo(next)) {
            error("dispute metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): DisputeMetric {
        return DisputeMetric(
            code = code,
            label = label,
            archived = archived,
            attributes = attributes,
            checksum = checksum,
            threshold = threshold,
            depot = depot,
            amount = amount,
            locale = locale,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dispute metric in status requested. */
fun newDisputeMetric(code: String, label: String): DisputeMetric {
    return DisputeMetric(
        code = code,
        label = label,
        archived = false,
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        threshold = 0.0,
        depot = "",
        amount = 0.0,
        locale = "",
        routeCode = "",
        status = DisputeMetricStatus.REQUESTED,
    )
}
