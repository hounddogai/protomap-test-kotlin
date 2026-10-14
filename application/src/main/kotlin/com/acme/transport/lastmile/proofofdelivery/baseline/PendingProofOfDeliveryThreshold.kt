package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending proof of delivery threshold is in its life cycle. */
enum class PendingProofOfDeliveryThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending proof of delivery threshold as the proof of delivery team keeps it. */
data class PendingProofOfDeliveryThreshold(
    var code: String,
    var name: String,
    var score: Double,
    var shiftCode: String,
    var graceDays: Int,
    var attributes: MutableMap<String, String>,
    var floorValue: Double,
    var status: PendingProofOfDeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingProofOfDeliveryThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingProofOfDeliveryThresholdStatus): Boolean {
        when (status) {
            PendingProofOfDeliveryThresholdStatus.IDLE -> {
                when (next) {
                    PendingProofOfDeliveryThresholdStatus.WARMING, PendingProofOfDeliveryThresholdStatus.DRAINING -> return true
                }
                return false
            }
            PendingProofOfDeliveryThresholdStatus.WARMING -> {
                when (next) {
                    PendingProofOfDeliveryThresholdStatus.READY -> return true
                }
                return false
            }
            PendingProofOfDeliveryThresholdStatus.READY -> {
                when (next) {
                    PendingProofOfDeliveryThresholdStatus.DRAINING, PendingProofOfDeliveryThresholdStatus.STOPPED -> return true
                }
                return false
            }
            PendingProofOfDeliveryThresholdStatus.DRAINING -> {
                when (next) {
                    PendingProofOfDeliveryThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingProofOfDeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending proof of delivery threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((score - graceDays.toDouble()) * 100) / 100

    fun snapshot(): PendingProofOfDeliveryThreshold {
        return PendingProofOfDeliveryThreshold(
            code = code,
            name = name,
            score = score,
            shiftCode = shiftCode,
            graceDays = graceDays,
            attributes = attributes,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending proof of delivery threshold in status idle. */
fun newPendingProofOfDeliveryThreshold(code: String, name: String): PendingProofOfDeliveryThreshold {
    return PendingProofOfDeliveryThreshold(
        code = code,
        name = name,
        score = 0.0,
        shiftCode = "",
        graceDays = 0,
        attributes = mutableMapOf<String, String>(),
        floorValue = 0.0,
        status = PendingProofOfDeliveryThresholdStatus.IDLE,
    )
}
