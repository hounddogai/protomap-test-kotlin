package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling proof of delivery quota is in its life cycle. */
enum class RollingProofOfDeliveryQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling proof of delivery quota, with what the planners need to act on it. */
data class RollingProofOfDeliveryQuota(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var attempts: Int,
    var driverId: String,
    var rate: Double,
    var vehicleId: String,
    var segment: String,
    var trailerId: String,
    var active: Boolean,
    var status: RollingProofOfDeliveryQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryQuotaStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryQuotaStatus.IDLE -> {
                when (next) {
                    RollingProofOfDeliveryQuotaStatus.STOPPED -> return true
                }
                return false
            }
            RollingProofOfDeliveryQuotaStatus.WARMING -> return next == RollingProofOfDeliveryQuotaStatus.READY || next == RollingProofOfDeliveryQuotaStatus.STOPPED
            RollingProofOfDeliveryQuotaStatus.READY -> {
                when (next) {
                    RollingProofOfDeliveryQuotaStatus.DRAINING, RollingProofOfDeliveryQuotaStatus.STOPPED -> return true
                }
                return false
            }
            RollingProofOfDeliveryQuotaStatus.DRAINING -> {
                when (next) {
                    RollingProofOfDeliveryQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((attempts.toDouble() + rate) * 100) / 100

    fun snapshot(): RollingProofOfDeliveryQuota {
        return RollingProofOfDeliveryQuota(
            code = code,
            title = title,
            pinned = pinned,
            attempts = attempts,
            driverId = driverId,
            rate = rate,
            vehicleId = vehicleId,
            segment = segment,
            trailerId = trailerId,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling proof of delivery quota in status idle. */
fun newRollingProofOfDeliveryQuota(code: String, title: String): RollingProofOfDeliveryQuota {
    return RollingProofOfDeliveryQuota(
        code = code,
        title = title,
        pinned = false,
        attempts = 0,
        driverId = "",
        rate = 0.0,
        vehicleId = "",
        segment = "",
        trailerId = "",
        active = false,
        status = RollingProofOfDeliveryQuotaStatus.IDLE,
    )
}
