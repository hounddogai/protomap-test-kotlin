package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive proof of delivery record is in its life cycle. */
enum class AdaptiveProofOfDeliveryRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive proof of delivery record as the proof of delivery team keeps it. */
data class AdaptiveProofOfDeliveryRecord(
    var code: String,
    var name: String,
    var timezone: String,
    var fuelLiters: Double,
    var phone: String,
    var revision: Int,
    var status: AdaptiveProofOfDeliveryRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveProofOfDeliveryRecordStatus): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryRecordStatus.PLANNED -> {
                when (next) {
                    AdaptiveProofOfDeliveryRecordStatus.SCHEDULED -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryRecordStatus.SCHEDULED -> return next == AdaptiveProofOfDeliveryRecordStatus.CANCELLED
            AdaptiveProofOfDeliveryRecordStatus.IN_PROGRESS -> return next == AdaptiveProofOfDeliveryRecordStatus.COMPLETED || next == AdaptiveProofOfDeliveryRecordStatus.CANCELLED
            AdaptiveProofOfDeliveryRecordStatus.COMPLETED -> return next == AdaptiveProofOfDeliveryRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveProofOfDeliveryRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive proof of delivery record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((fuelLiters + revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveProofOfDeliveryRecord {
        return AdaptiveProofOfDeliveryRecord(
            code = code,
            name = name,
            timezone = timezone,
            fuelLiters = fuelLiters,
            phone = phone,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive proof of delivery record in status planned. */
fun newAdaptiveProofOfDeliveryRecord(code: String, name: String): AdaptiveProofOfDeliveryRecord {
    return AdaptiveProofOfDeliveryRecord(
        code = code,
        name = name,
        timezone = "",
        fuelLiters = 0.0,
        phone = "",
        revision = 0,
        status = AdaptiveProofOfDeliveryRecordStatus.PLANNED,
    )
}
