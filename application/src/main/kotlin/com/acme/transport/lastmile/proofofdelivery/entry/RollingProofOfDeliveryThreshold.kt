package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling proof of delivery threshold is in its life cycle. */
enum class RollingProofOfDeliveryThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling proof of delivery threshold as the proof of delivery team keeps it. */
data class RollingProofOfDeliveryThreshold(
    var code: String,
    var name: String,
    var unit: String,
    var checksum: String,
    var createdAt: Int,
    var rate: Double,
    var ceiling: Double,
    var status: RollingProofOfDeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryThresholdStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryThresholdStatus.IDLE -> return next == RollingProofOfDeliveryThresholdStatus.DRAINING
            RollingProofOfDeliveryThresholdStatus.WARMING -> return next == RollingProofOfDeliveryThresholdStatus.DRAINING || next == RollingProofOfDeliveryThresholdStatus.STOPPED
            RollingProofOfDeliveryThresholdStatus.READY -> return next == RollingProofOfDeliveryThresholdStatus.STOPPED
            RollingProofOfDeliveryThresholdStatus.DRAINING -> {
                when (next) {
                    RollingProofOfDeliveryThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling proof of delivery threshold"} $code"

    fun netLoad(): Double = round((ceiling + rate) * 100) / 100

    fun snapshot(): RollingProofOfDeliveryThreshold {
        return RollingProofOfDeliveryThreshold(
            code = code,
            name = name,
            unit = unit,
            checksum = checksum,
            createdAt = createdAt,
            rate = rate,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling proof of delivery threshold in status idle. */
fun newRollingProofOfDeliveryThreshold(code: String, name: String): RollingProofOfDeliveryThreshold {
    return RollingProofOfDeliveryThreshold(
        code = code,
        name = name,
        unit = "",
        checksum = "",
        createdAt = 0,
        rate = 0.0,
        ceiling = 0.0,
        status = RollingProofOfDeliveryThresholdStatus.IDLE,
    )
}
