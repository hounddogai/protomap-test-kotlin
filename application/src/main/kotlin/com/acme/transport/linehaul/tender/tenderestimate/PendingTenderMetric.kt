package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tender metric is in its life cycle. */
enum class PendingTenderMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending tender metric. Its code is unique within a tenant. */
data class PendingTenderMetric(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var updatedAt: Int,
    var summary: String,
    var ceiling: Double,
    var distanceKm: Double,
    var status: PendingTenderMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderMetricStatus): Boolean {
        when (status) {
            PendingTenderMetricStatus.CREATED -> return next == PendingTenderMetricStatus.VALIDATED
            PendingTenderMetricStatus.VALIDATED -> {
                when (next) {
                    PendingTenderMetricStatus.PUBLISHED, PendingTenderMetricStatus.RETIRED -> return true
                }
                return false
            }
            PendingTenderMetricStatus.PUBLISHED -> return next == PendingTenderMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingTenderMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending tender metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending tender metric"} $code"

    fun weightedLoad(): Double = round((distanceKm + ceiling) * 100) / 100

    fun snapshot(): PendingTenderMetric {
        return PendingTenderMetric(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            updatedAt = updatedAt,
            summary = summary,
            ceiling = ceiling,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending tender metric in status created. */
fun newPendingTenderMetric(code: String, name: String): PendingTenderMetric {
    return PendingTenderMetric(
        code = code,
        name = name,
        fuelLiters = 0.0,
        updatedAt = 0,
        summary = "",
        ceiling = 0.0,
        distanceKm = 0.0,
        status = PendingTenderMetricStatus.CREATED,
    )
}
