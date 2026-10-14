package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched zone variance is in its life cycle. */
enum class BatchedZoneVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched zone variance, with what the planners need to act on it. */
data class BatchedZoneVariance(
    var code: String,
    var name: String,
    var weight: Double,
    var visible: Boolean,
    var batchSize: Int,
    var carrier: String,
    var minimum: Double,
    var capacity: Int,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var status: BatchedZoneVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedZoneVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedZoneVarianceStatus): Boolean {
        when (status) {
            BatchedZoneVarianceStatus.PLANNED -> {
                when (next) {
                    BatchedZoneVarianceStatus.SCHEDULED, BatchedZoneVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedZoneVarianceStatus.SCHEDULED -> return next == BatchedZoneVarianceStatus.COMPLETED || next == BatchedZoneVarianceStatus.CANCELLED
            BatchedZoneVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedZoneVarianceStatus.COMPLETED, BatchedZoneVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedZoneVarianceStatus.COMPLETED -> return next == BatchedZoneVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedZoneVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched zone variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(minimum * weight * 100) / 100

    fun snapshot(): BatchedZoneVariance {
        return BatchedZoneVariance(
            code = code,
            name = name,
            weight = weight,
            visible = visible,
            batchSize = batchSize,
            carrier = carrier,
            minimum = minimum,
            capacity = capacity,
            phone = phone,
            attributes = attributes,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched zone variance in status planned. */
fun newBatchedZoneVariance(code: String, name: String): BatchedZoneVariance {
    return BatchedZoneVariance(
        code = code,
        name = name,
        weight = 0.0,
        visible = false,
        batchSize = 0,
        carrier = "",
        minimum = 0.0,
        capacity = 0,
        phone = "",
        attributes = mutableMapOf<String, String>(),
        segment = "",
        status = BatchedZoneVarianceStatus.PLANNED,
    )
}
