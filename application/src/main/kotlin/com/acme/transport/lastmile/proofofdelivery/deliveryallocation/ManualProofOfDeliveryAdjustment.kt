package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual proof of delivery adjustment is in its life cycle. */
enum class ManualProofOfDeliveryAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual proof of delivery adjustment and its current status. */
data class ManualProofOfDeliveryAdjustment(
    var code: String,
    var title: String,
    var timezone: String,
    var version: Int,
    var source: String,
    var weight: Double,
    var floorValue: Double,
    var approved: Boolean,
    var status: ManualProofOfDeliveryAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryAdjustmentStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryAdjustmentStatus.PLANNED -> return next == ManualProofOfDeliveryAdjustmentStatus.COMPLETED || next == ManualProofOfDeliveryAdjustmentStatus.CANCELLED
            ManualProofOfDeliveryAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    ManualProofOfDeliveryAdjustmentStatus.IN_PROGRESS, ManualProofOfDeliveryAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            ManualProofOfDeliveryAdjustmentStatus.IN_PROGRESS -> return next == ManualProofOfDeliveryAdjustmentStatus.COMPLETED || next == ManualProofOfDeliveryAdjustmentStatus.CANCELLED
            ManualProofOfDeliveryAdjustmentStatus.COMPLETED -> {
                when (next) {
                    ManualProofOfDeliveryAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"manual proof of delivery adjustment"} $code"

    fun adjustedVolume(): Double = round(floorValue * version.toDouble() * 100) / 100

    fun snapshot(): ManualProofOfDeliveryAdjustment {
        return ManualProofOfDeliveryAdjustment(
            code = code,
            title = title,
            timezone = timezone,
            version = version,
            source = source,
            weight = weight,
            floorValue = floorValue,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual proof of delivery adjustment in status planned. */
fun newManualProofOfDeliveryAdjustment(code: String, title: String): ManualProofOfDeliveryAdjustment {
    return ManualProofOfDeliveryAdjustment(
        code = code,
        title = title,
        timezone = "",
        version = 0,
        source = "",
        weight = 0.0,
        floorValue = 0.0,
        approved = false,
        status = ManualProofOfDeliveryAdjustmentStatus.PLANNED,
    )
}
