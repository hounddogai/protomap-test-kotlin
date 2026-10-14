package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending defect metric records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a pending defect metric is in its life cycle. */
enum class PendingDefectMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending defect metric as the defect team keeps it. */
data class PendingDefectMetric(
    var code: String,
    var name: String,
    var routeCode: String,
    var depot: String,
    var phone: String,
    var threshold: Double,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var currency: String,
    var amount: Double,
    var status: PendingDefectMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDefectMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDefectMetricStatus): Boolean {
        when (status) {
            PendingDefectMetricStatus.OPEN -> return next == PendingDefectMetricStatus.REOPENED
            PendingDefectMetricStatus.IN_REVIEW -> return next == PendingDefectMetricStatus.REOPENED
            PendingDefectMetricStatus.RESOLVED -> return next == PendingDefectMetricStatus.REOPENED
            PendingDefectMetricStatus.REOPENED -> {
                when (next) {
                    PendingDefectMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDefectMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending defect metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((threshold + amount) * 100) / 100

    fun snapshot(): PendingDefectMetric {
        return PendingDefectMetric(
            code = code,
            name = name,
            routeCode = routeCode,
            depot = depot,
            phone = phone,
            threshold = threshold,
            attributes = attributes,
            contactEmail = contactEmail,
            currency = currency,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending defect metric in status open. */
fun newPendingDefectMetric(code: String, name: String): PendingDefectMetric {
    return PendingDefectMetric(
        code = code,
        name = name,
        routeCode = "",
        depot = "",
        phone = "",
        threshold = 0.0,
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        currency = "",
        amount = 0.0,
        status = PendingDefectMetricStatus.OPEN,
    )
}
