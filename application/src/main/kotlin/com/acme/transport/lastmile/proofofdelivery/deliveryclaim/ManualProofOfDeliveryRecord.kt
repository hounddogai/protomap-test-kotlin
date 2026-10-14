package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.normalize

/** Where a manual proof of delivery record is in its life cycle. */
enum class ManualProofOfDeliveryRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual proof of delivery record as the proof of delivery team keeps it. */
data class ManualProofOfDeliveryRecord(
    var code: String,
    var title: String,
    var active: Boolean,
    var source: String,
    var segment: String,
    var notes: String,
    var version: Int,
    var status: ManualProofOfDeliveryRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryRecordStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryRecordStatus.REQUESTED -> return next == ManualProofOfDeliveryRecordStatus.EXPIRED
            ManualProofOfDeliveryRecordStatus.GRANTED -> return next == ManualProofOfDeliveryRecordStatus.EXPIRED
            ManualProofOfDeliveryRecordStatus.REVOKED -> {
                when (next) {
                    ManualProofOfDeliveryRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryRecordStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"manual proof of delivery record"} $code"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): ManualProofOfDeliveryRecord {
        return ManualProofOfDeliveryRecord(
            code = code,
            title = title,
            active = active,
            source = source,
            segment = segment,
            notes = notes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual proof of delivery record in status requested. */
fun newManualProofOfDeliveryRecord(code: String, title: String): ManualProofOfDeliveryRecord {
    return ManualProofOfDeliveryRecord(
        code = code,
        title = title,
        active = false,
        source = "",
        segment = "",
        notes = "",
        version = 0,
        status = ManualProofOfDeliveryRecordStatus.REQUESTED,
    )
}
