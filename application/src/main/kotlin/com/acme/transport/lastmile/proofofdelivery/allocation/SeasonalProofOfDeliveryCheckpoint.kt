package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal proof of delivery checkpoint is in its life cycle. */
enum class SeasonalProofOfDeliveryCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a seasonal proof of delivery checkpoint and its current status. */
data class SeasonalProofOfDeliveryCheckpoint(
    var code: String,
    var label: String,
    var discount: Double,
    var weight: Double,
    var driverId: String,
    var odometer: Int,
    var phone: String,
    var timezone: String,
    var limit: Int,
    var contactEmail: String,
    var status: SeasonalProofOfDeliveryCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalProofOfDeliveryCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalProofOfDeliveryCheckpointStatus): Boolean {
        when (status) {
            SeasonalProofOfDeliveryCheckpointStatus.PROPOSED -> return next == SeasonalProofOfDeliveryCheckpointStatus.APPROVED || next == SeasonalProofOfDeliveryCheckpointStatus.WITHDRAWN
            SeasonalProofOfDeliveryCheckpointStatus.APPROVED -> {
                when (next) {
                    SeasonalProofOfDeliveryCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalProofOfDeliveryCheckpointStatus.REJECTED -> {
                when (next) {
                    SeasonalProofOfDeliveryCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalProofOfDeliveryCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("seasonal proof of delivery checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round((discount + weight) * 100) / 100

    fun snapshot(): SeasonalProofOfDeliveryCheckpoint {
        return SeasonalProofOfDeliveryCheckpoint(
            code = code,
            label = label,
            discount = discount,
            weight = weight,
            driverId = driverId,
            odometer = odometer,
            phone = phone,
            timezone = timezone,
            limit = limit,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal proof of delivery checkpoint in status proposed. */
fun newSeasonalProofOfDeliveryCheckpoint(code: String, label: String): SeasonalProofOfDeliveryCheckpoint {
    return SeasonalProofOfDeliveryCheckpoint(
        code = code,
        label = label,
        discount = 0.0,
        weight = 0.0,
        driverId = "",
        odometer = 0,
        phone = "",
        timezone = "",
        limit = 0,
        contactEmail = "",
        status = SeasonalProofOfDeliveryCheckpointStatus.PROPOSED,
    )
}
