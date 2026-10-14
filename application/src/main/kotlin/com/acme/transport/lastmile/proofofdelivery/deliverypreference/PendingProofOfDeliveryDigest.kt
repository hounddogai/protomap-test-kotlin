package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending proof of delivery digest is in its life cycle. */
enum class PendingProofOfDeliveryDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending proof of delivery digest. Its code is unique within a tenant. */
data class PendingProofOfDeliveryDigest(
    var code: String,
    var label: String,
    var priority: Int,
    var name: String,
    var durationMinutes: Int,
    var status: PendingProofOfDeliveryDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingProofOfDeliveryDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingProofOfDeliveryDigestStatus): Boolean {
        when (status) {
            PendingProofOfDeliveryDigestStatus.IDLE -> {
                when (next) {
                    PendingProofOfDeliveryDigestStatus.DRAINING, PendingProofOfDeliveryDigestStatus.STOPPED -> return true
                }
                return false
            }
            PendingProofOfDeliveryDigestStatus.WARMING -> return next == PendingProofOfDeliveryDigestStatus.STOPPED
            PendingProofOfDeliveryDigestStatus.READY -> {
                when (next) {
                    PendingProofOfDeliveryDigestStatus.STOPPED -> return true
                }
                return false
            }
            PendingProofOfDeliveryDigestStatus.DRAINING -> {
                when (next) {
                    PendingProofOfDeliveryDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingProofOfDeliveryDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending proof of delivery digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((priority.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingProofOfDeliveryDigest {
        return PendingProofOfDeliveryDigest(
            code = code,
            label = label,
            priority = priority,
            name = name,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending proof of delivery digest in status idle. */
fun newPendingProofOfDeliveryDigest(code: String, label: String): PendingProofOfDeliveryDigest {
    return PendingProofOfDeliveryDigest(
        code = code,
        label = label,
        priority = 0,
        name = "",
        durationMinutes = 0,
        status = PendingProofOfDeliveryDigestStatus.IDLE,
    )
}
