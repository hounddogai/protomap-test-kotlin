package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary proof of delivery record records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a primary proof of delivery record is in its life cycle. */
enum class PrimaryProofOfDeliveryRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary proof of delivery record, with what the planners need to act on it. */
data class PrimaryProofOfDeliveryRecord(
    var code: String,
    var title: String,
    var phone: String,
    var pallets: Int,
    var revision: Int,
    var source: String,
    var vehicleId: String,
    var unit: String,
    var score: Double,
    var loadKg: Double,
    var depot: String,
    var status: PrimaryProofOfDeliveryRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryProofOfDeliveryRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryProofOfDeliveryRecordStatus): Boolean {
        when (status) {
            PrimaryProofOfDeliveryRecordStatus.NEW -> return next == PrimaryProofOfDeliveryRecordStatus.RUNNING || next == PrimaryProofOfDeliveryRecordStatus.DONE
            PrimaryProofOfDeliveryRecordStatus.QUEUED -> return next == PrimaryProofOfDeliveryRecordStatus.RUNNING
            PrimaryProofOfDeliveryRecordStatus.RUNNING -> return next == PrimaryProofOfDeliveryRecordStatus.DONE
            PrimaryProofOfDeliveryRecordStatus.DONE -> {
                when (next) {
                    PrimaryProofOfDeliveryRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryProofOfDeliveryRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary proof of delivery record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round(pallets.toDouble() * loadKg * 100) / 100

    fun snapshot(): PrimaryProofOfDeliveryRecord {
        return PrimaryProofOfDeliveryRecord(
            code = code,
            title = title,
            phone = phone,
            pallets = pallets,
            revision = revision,
            source = source,
            vehicleId = vehicleId,
            unit = unit,
            score = score,
            loadKg = loadKg,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary proof of delivery record in status new. */
fun newPrimaryProofOfDeliveryRecord(code: String, title: String): PrimaryProofOfDeliveryRecord {
    return PrimaryProofOfDeliveryRecord(
        code = code,
        title = title,
        phone = "",
        pallets = 0,
        revision = 0,
        source = "",
        vehicleId = "",
        unit = "",
        score = 0.0,
        loadKg = 0.0,
        depot = "",
        status = PrimaryProofOfDeliveryRecordStatus.NEW,
    )
}
