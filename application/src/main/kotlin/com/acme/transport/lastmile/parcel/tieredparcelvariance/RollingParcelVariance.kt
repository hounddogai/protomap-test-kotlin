package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling parcel variance records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a rolling parcel variance is in its life cycle. */
enum class RollingParcelVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rolling parcel variance, with what the planners need to act on it. */
data class RollingParcelVariance(
    var code: String,
    var label: String,
    var ratio: Double,
    var createdAt: Int,
    var vehicleId: String,
    var attempts: Int,
    var status: RollingParcelVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingParcelVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingParcelVarianceStatus): Boolean {
        when (status) {
            RollingParcelVarianceStatus.DRAFT -> return next == RollingParcelVarianceStatus.SUSPENDED
            RollingParcelVarianceStatus.PENDING -> return next == RollingParcelVarianceStatus.ACTIVE
            RollingParcelVarianceStatus.ACTIVE -> {
                when (next) {
                    RollingParcelVarianceStatus.SUSPENDED, RollingParcelVarianceStatus.CLOSED -> return true
                }
                return false
            }
            RollingParcelVarianceStatus.SUSPENDED -> return next == RollingParcelVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingParcelVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rolling parcel variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round(ratio * attempts.toDouble() * 100) / 100

    fun snapshot(): RollingParcelVariance {
        return RollingParcelVariance(
            code = code,
            label = label,
            ratio = ratio,
            createdAt = createdAt,
            vehicleId = vehicleId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling parcel variance in status draft. */
fun newRollingParcelVariance(code: String, label: String): RollingParcelVariance {
    return RollingParcelVariance(
        code = code,
        label = label,
        ratio = 0.0,
        createdAt = 0,
        vehicleId = "",
        attempts = 0,
        status = RollingParcelVarianceStatus.DRAFT,
    )
}
