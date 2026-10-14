package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global proof of delivery record records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a global proof of delivery record is in its life cycle. */
enum class GlobalProofOfDeliveryRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global proof of delivery record. Its code is unique within a tenant. */
data class GlobalProofOfDeliveryRecord(
    var code: String,
    var title: String,
    var odometer: Int,
    var active: Boolean,
    var attempts: Int,
    var category: String,
    var status: GlobalProofOfDeliveryRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalProofOfDeliveryRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalProofOfDeliveryRecordStatus): Boolean {
        when (status) {
            GlobalProofOfDeliveryRecordStatus.CREATED -> return next == GlobalProofOfDeliveryRecordStatus.PUBLISHED || next == GlobalProofOfDeliveryRecordStatus.RETIRED
            GlobalProofOfDeliveryRecordStatus.VALIDATED -> {
                when (next) {
                    GlobalProofOfDeliveryRecordStatus.PUBLISHED, GlobalProofOfDeliveryRecordStatus.RETIRED -> return true
                }
                return false
            }
            GlobalProofOfDeliveryRecordStatus.PUBLISHED -> {
                when (next) {
                    GlobalProofOfDeliveryRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalProofOfDeliveryRecordStatus) {
        if (!canMoveTo(next)) {
            error("global proof of delivery record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(odometer.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): GlobalProofOfDeliveryRecord {
        return GlobalProofOfDeliveryRecord(
            code = code,
            title = title,
            odometer = odometer,
            active = active,
            attempts = attempts,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global proof of delivery record in status created. */
fun newGlobalProofOfDeliveryRecord(code: String, title: String): GlobalProofOfDeliveryRecord {
    return GlobalProofOfDeliveryRecord(
        code = code,
        title = title,
        odometer = 0,
        active = false,
        attempts = 0,
        category = "",
        status = GlobalProofOfDeliveryRecordStatus.CREATED,
    )
}
