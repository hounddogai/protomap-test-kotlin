package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive camera event rollup is in its life cycle. */
enum class AdaptiveCameraEventRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive camera event rollup. Its code is unique within a tenant. */
data class AdaptiveCameraEventRollup(
    var code: String,
    var name: String,
    var archived: Boolean,
    var discount: Double,
    var ceiling: Double,
    var threshold: Double,
    var phone: String,
    var budget: Double,
    var etaMinutes: Int,
    var carrier: String,
    var status: AdaptiveCameraEventRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCameraEventRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCameraEventRollupStatus): Boolean {
        when (status) {
            AdaptiveCameraEventRollupStatus.PLANNED -> return next == AdaptiveCameraEventRollupStatus.SCHEDULED
            AdaptiveCameraEventRollupStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveCameraEventRollupStatus.IN_PROGRESS, AdaptiveCameraEventRollupStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveCameraEventRollupStatus.IN_PROGRESS -> return next == AdaptiveCameraEventRollupStatus.COMPLETED || next == AdaptiveCameraEventRollupStatus.CANCELLED
            AdaptiveCameraEventRollupStatus.COMPLETED -> return next == AdaptiveCameraEventRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveCameraEventRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive camera event rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((threshold + discount) * 100) / 100

    fun snapshot(): AdaptiveCameraEventRollup {
        return AdaptiveCameraEventRollup(
            code = code,
            name = name,
            archived = archived,
            discount = discount,
            ceiling = ceiling,
            threshold = threshold,
            phone = phone,
            budget = budget,
            etaMinutes = etaMinutes,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive camera event rollup in status planned. */
fun newAdaptiveCameraEventRollup(code: String, name: String): AdaptiveCameraEventRollup {
    return AdaptiveCameraEventRollup(
        code = code,
        name = name,
        archived = false,
        discount = 0.0,
        ceiling = 0.0,
        threshold = 0.0,
        phone = "",
        budget = 0.0,
        etaMinutes = 0,
        carrier = "",
        status = AdaptiveCameraEventRollupStatus.PLANNED,
    )
}
