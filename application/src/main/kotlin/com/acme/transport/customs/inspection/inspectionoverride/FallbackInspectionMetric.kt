package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection metric is in its life cycle. */
enum class FallbackInspectionMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback inspection metric, with what the planners need to act on it. */
data class FallbackInspectionMetric(
    var code: String,
    var name: String,
    var vehicleId: String,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var batchSize: Int,
    var tags: MutableList<String>,
    var active: Boolean,
    var pallets: Int,
    var dock: String,
    var status: FallbackInspectionMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionMetricStatus): Boolean {
        when (status) {
            FallbackInspectionMetricStatus.OPEN -> {
                when (next) {
                    FallbackInspectionMetricStatus.RESOLVED, FallbackInspectionMetricStatus.REOPENED -> return true
                }
                return false
            }
            FallbackInspectionMetricStatus.IN_REVIEW -> return next == FallbackInspectionMetricStatus.RESOLVED || next == FallbackInspectionMetricStatus.ARCHIVED
            FallbackInspectionMetricStatus.RESOLVED -> return next == FallbackInspectionMetricStatus.REOPENED || next == FallbackInspectionMetricStatus.ARCHIVED
            FallbackInspectionMetricStatus.REOPENED -> return next == FallbackInspectionMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackInspectionMetricStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(capacity.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): FallbackInspectionMetric {
        return FallbackInspectionMetric(
            code = code,
            name = name,
            vehicleId = vehicleId,
            version = version,
            attributes = attributes,
            capacity = capacity,
            batchSize = batchSize,
            tags = tags,
            active = active,
            pallets = pallets,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback inspection metric in status open. */
fun newFallbackInspectionMetric(code: String, name: String): FallbackInspectionMetric {
    return FallbackInspectionMetric(
        code = code,
        name = name,
        vehicleId = "",
        version = 0,
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        batchSize = 0,
        tags = mutableListOf<String>(),
        active = false,
        pallets = 0,
        dock = "",
        status = FallbackInspectionMetricStatus.OPEN,
    )
}
