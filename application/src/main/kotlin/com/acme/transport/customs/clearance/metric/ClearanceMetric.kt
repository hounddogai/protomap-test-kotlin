package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance metric records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance metric is in its life cycle. */
enum class ClearanceMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A clearance metric as the clearance team keeps it. */
data class ClearanceMetric(
    var code: String,
    var title: String,
    var revision: Int,
    var threshold: Double,
    var stopCount: Int,
    var vehicleId: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var summary: String,
    var status: ClearanceMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceMetricStatus): Boolean {
        when (status) {
            ClearanceMetricStatus.DRAFT -> {
                when (next) {
                    ClearanceMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            ClearanceMetricStatus.PENDING -> return next == ClearanceMetricStatus.CLOSED
            ClearanceMetricStatus.ACTIVE -> return next == ClearanceMetricStatus.SUSPENDED || next == ClearanceMetricStatus.CLOSED
            ClearanceMetricStatus.SUSPENDED -> {
                when (next) {
                    ClearanceMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceMetricStatus) {
        if (!canMoveTo(next)) {
            error("clearance metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(stopCount.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): ClearanceMetric {
        return ClearanceMetric(
            code = code,
            title = title,
            revision = revision,
            threshold = threshold,
            stopCount = stopCount,
            vehicleId = vehicleId,
            tags = tags,
            reviewer = reviewer,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance metric in status draft. */
fun newClearanceMetric(code: String, title: String): ClearanceMetric {
    return ClearanceMetric(
        code = code,
        title = title,
        revision = 0,
        threshold = 0.0,
        stopCount = 0,
        vehicleId = "",
        tags = mutableListOf<String>(),
        reviewer = "",
        summary = "",
        status = ClearanceMetricStatus.DRAFT,
    )
}
