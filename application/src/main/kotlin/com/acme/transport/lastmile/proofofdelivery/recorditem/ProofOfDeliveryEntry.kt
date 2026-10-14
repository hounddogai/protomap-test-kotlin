package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery entry is in its life cycle. */
enum class ProofOfDeliveryEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A proof of delivery entry as the proof of delivery team keeps it. */
data class ProofOfDeliveryEntry(
    var code: String,
    var label: String,
    var discount: Double,
    var floorValue: Double,
    var pinned: Boolean,
    var status: ProofOfDeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryEntryStatus): Boolean {
        when (status) {
            ProofOfDeliveryEntryStatus.CREATED -> {
                when (next) {
                    ProofOfDeliveryEntryStatus.RETIRED -> return true
                }
                return false
            }
            ProofOfDeliveryEntryStatus.VALIDATED -> return next == ProofOfDeliveryEntryStatus.PUBLISHED || next == ProofOfDeliveryEntryStatus.RETIRED
            ProofOfDeliveryEntryStatus.PUBLISHED -> {
                when (next) {
                    ProofOfDeliveryEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"proof of delivery entry"} $code"

    fun weightedVolume(): Double = round((discount - floorValue) * 100) / 100

    fun snapshot(): ProofOfDeliveryEntry {
        return ProofOfDeliveryEntry(
            code = code,
            label = label,
            discount = discount,
            floorValue = floorValue,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery entry in status created. */
fun newProofOfDeliveryEntry(code: String, label: String): ProofOfDeliveryEntry {
    return ProofOfDeliveryEntry(
        code = code,
        label = label,
        discount = 0.0,
        floorValue = 0.0,
        pinned = false,
        status = ProofOfDeliveryEntryStatus.CREATED,
    )
}
