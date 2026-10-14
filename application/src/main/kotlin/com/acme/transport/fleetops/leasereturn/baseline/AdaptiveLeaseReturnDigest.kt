package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lease return digest is in its life cycle. */
enum class AdaptiveLeaseReturnDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive lease return digest and its current status. */
data class AdaptiveLeaseReturnDigest(
    var code: String,
    var title: String,
    var stopCount: Int,
    var label: String,
    var name: String,
    var phone: String,
    var vehicleId: String,
    var ceiling: Double,
    var status: AdaptiveLeaseReturnDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnDigestStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnDigestStatus.IDLE -> return next == AdaptiveLeaseReturnDigestStatus.READY || next == AdaptiveLeaseReturnDigestStatus.STOPPED
            AdaptiveLeaseReturnDigestStatus.WARMING -> return next == AdaptiveLeaseReturnDigestStatus.READY || next == AdaptiveLeaseReturnDigestStatus.DRAINING
            AdaptiveLeaseReturnDigestStatus.READY -> {
                when (next) {
                    AdaptiveLeaseReturnDigestStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveLeaseReturnDigestStatus.DRAINING -> {
                when (next) {
                    AdaptiveLeaseReturnDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnDigestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive lease return digest"} $code"

    fun netExposure(): Double = round((stopCount.toDouble() + ceiling) * 100) / 100

    fun snapshot(): AdaptiveLeaseReturnDigest {
        return AdaptiveLeaseReturnDigest(
            code = code,
            title = title,
            stopCount = stopCount,
            label = label,
            name = name,
            phone = phone,
            vehicleId = vehicleId,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive lease return digest in status idle. */
fun newAdaptiveLeaseReturnDigest(code: String, title: String): AdaptiveLeaseReturnDigest {
    return AdaptiveLeaseReturnDigest(
        code = code,
        title = title,
        stopCount = 0,
        label = "",
        name = "",
        phone = "",
        vehicleId = "",
        ceiling = 0.0,
        status = AdaptiveLeaseReturnDigestStatus.IDLE,
    )
}
