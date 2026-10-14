package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy proof of delivery window is in its life cycle. */
enum class LegacyProofOfDeliveryWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy proof of delivery window and its current status. */
data class LegacyProofOfDeliveryWindow(
    var code: String,
    var title: String,
    var discount: Double,
    var stopCount: Int,
    var retries: Int,
    var trailerId: String,
    var capacity: Int,
    var fuelLiters: Double,
    var status: LegacyProofOfDeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyProofOfDeliveryWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyProofOfDeliveryWindowStatus): Boolean {
        when (status) {
            LegacyProofOfDeliveryWindowStatus.OPEN -> return next == LegacyProofOfDeliveryWindowStatus.RESOLVED || next == LegacyProofOfDeliveryWindowStatus.REOPENED
            LegacyProofOfDeliveryWindowStatus.IN_REVIEW -> {
                when (next) {
                    LegacyProofOfDeliveryWindowStatus.REOPENED, LegacyProofOfDeliveryWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyProofOfDeliveryWindowStatus.RESOLVED -> return next == LegacyProofOfDeliveryWindowStatus.REOPENED || next == LegacyProofOfDeliveryWindowStatus.ARCHIVED
            LegacyProofOfDeliveryWindowStatus.REOPENED -> {
                when (next) {
                    LegacyProofOfDeliveryWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyProofOfDeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("legacy proof of delivery window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round(capacity.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): LegacyProofOfDeliveryWindow {
        return LegacyProofOfDeliveryWindow(
            code = code,
            title = title,
            discount = discount,
            stopCount = stopCount,
            retries = retries,
            trailerId = trailerId,
            capacity = capacity,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy proof of delivery window in status open. */
fun newLegacyProofOfDeliveryWindow(code: String, title: String): LegacyProofOfDeliveryWindow {
    return LegacyProofOfDeliveryWindow(
        code = code,
        title = title,
        discount = 0.0,
        stopCount = 0,
        retries = 0,
        trailerId = "",
        capacity = 0,
        fuelLiters = 0.0,
        status = LegacyProofOfDeliveryWindowStatus.OPEN,
    )
}
