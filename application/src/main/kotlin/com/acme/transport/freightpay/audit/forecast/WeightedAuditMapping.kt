package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted audit mapping is in its life cycle. */
enum class WeightedAuditMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted audit mapping and its current status. */
data class WeightedAuditMapping(
    var code: String,
    var title: String,
    var weight: Double,
    var distanceKm: Double,
    var shiftCode: String,
    var stopCount: Int,
    var status: WeightedAuditMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAuditMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAuditMappingStatus): Boolean {
        when (status) {
            WeightedAuditMappingStatus.PLANNED -> {
                when (next) {
                    WeightedAuditMappingStatus.SCHEDULED, WeightedAuditMappingStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedAuditMappingStatus.SCHEDULED -> return next == WeightedAuditMappingStatus.IN_PROGRESS
            WeightedAuditMappingStatus.IN_PROGRESS -> return next == WeightedAuditMappingStatus.CANCELLED
            WeightedAuditMappingStatus.COMPLETED -> return next == WeightedAuditMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedAuditMappingStatus) {
        if (!canMoveTo(next)) {
            error("weighted audit mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted audit mapping"} $code"

    fun weightedExposure(): Double = round((weight + distanceKm) * 100) / 100

    fun snapshot(): WeightedAuditMapping {
        return WeightedAuditMapping(
            code = code,
            title = title,
            weight = weight,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted audit mapping in status planned. */
fun newWeightedAuditMapping(code: String, title: String): WeightedAuditMapping {
    return WeightedAuditMapping(
        code = code,
        title = title,
        weight = 0.0,
        distanceKm = 0.0,
        shiftCode = "",
        stopCount = 0,
        status = WeightedAuditMappingStatus.PLANNED,
    )
}
