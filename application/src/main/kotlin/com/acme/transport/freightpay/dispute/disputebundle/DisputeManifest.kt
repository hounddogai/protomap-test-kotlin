package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute manifest is in its life cycle. */
enum class DisputeManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One dispute manifest, with what the planners need to act on it. */
data class DisputeManifest(
    var code: String,
    var title: String,
    var stopCount: Int,
    var attempts: Int,
    var limit: Int,
    var quantity: Int,
    var ownerId: String,
    var phone: String,
    var region: String,
    var status: DisputeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeManifestStatus): Boolean {
        when (status) {
            DisputeManifestStatus.IDLE -> {
                when (next) {
                    DisputeManifestStatus.WARMING, DisputeManifestStatus.DRAINING -> return true
                }
                return false
            }
            DisputeManifestStatus.WARMING -> return next == DisputeManifestStatus.READY
            DisputeManifestStatus.READY -> return next == DisputeManifestStatus.DRAINING || next == DisputeManifestStatus.STOPPED
            DisputeManifestStatus.DRAINING -> return next == DisputeManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DisputeManifestStatus) {
        if (!canMoveTo(next)) {
            error("dispute manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dispute manifest"} $code"

    fun adjustedExposure(): Double = round(stopCount.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): DisputeManifest {
        return DisputeManifest(
            code = code,
            title = title,
            stopCount = stopCount,
            attempts = attempts,
            limit = limit,
            quantity = quantity,
            ownerId = ownerId,
            phone = phone,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dispute manifest in status idle. */
fun newDisputeManifest(code: String, title: String): DisputeManifest {
    return DisputeManifest(
        code = code,
        title = title,
        stopCount = 0,
        attempts = 0,
        limit = 0,
        quantity = 0,
        ownerId = "",
        phone = "",
        region = "",
        status = DisputeManifestStatus.IDLE,
    )
}
