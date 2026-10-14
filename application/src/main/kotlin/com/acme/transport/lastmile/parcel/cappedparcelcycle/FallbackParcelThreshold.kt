package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback parcel threshold is in its life cycle. */
enum class FallbackParcelThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fallback parcel threshold and its current status. */
data class FallbackParcelThreshold(
    var code: String,
    var name: String,
    var threshold: Double,
    var vehicleId: String,
    var etaMinutes: Int,
    var updatedAt: Int,
    var quantity: Int,
    var durationMinutes: Int,
    var createdAt: Int,
    var graceDays: Int,
    var status: FallbackParcelThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackParcelThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackParcelThresholdStatus): Boolean {
        when (status) {
            FallbackParcelThresholdStatus.DRAFT -> return next == FallbackParcelThresholdStatus.PENDING || next == FallbackParcelThresholdStatus.CLOSED
            FallbackParcelThresholdStatus.PENDING -> return next == FallbackParcelThresholdStatus.SUSPENDED || next == FallbackParcelThresholdStatus.CLOSED
            FallbackParcelThresholdStatus.ACTIVE -> {
                when (next) {
                    FallbackParcelThresholdStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackParcelThresholdStatus.SUSPENDED -> {
                when (next) {
                    FallbackParcelThresholdStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackParcelThresholdStatus) {
        if (!canMoveTo(next)) {
            error("fallback parcel threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback parcel threshold"} $code"

    fun adjustedExposure(): Double = round((threshold + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackParcelThreshold {
        return FallbackParcelThreshold(
            code = code,
            name = name,
            threshold = threshold,
            vehicleId = vehicleId,
            etaMinutes = etaMinutes,
            updatedAt = updatedAt,
            quantity = quantity,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback parcel threshold in status draft. */
fun newFallbackParcelThreshold(code: String, name: String): FallbackParcelThreshold {
    return FallbackParcelThreshold(
        code = code,
        name = name,
        threshold = 0.0,
        vehicleId = "",
        etaMinutes = 0,
        updatedAt = 0,
        quantity = 0,
        durationMinutes = 0,
        createdAt = 0,
        graceDays = 0,
        status = FallbackParcelThresholdStatus.DRAFT,
    )
}
