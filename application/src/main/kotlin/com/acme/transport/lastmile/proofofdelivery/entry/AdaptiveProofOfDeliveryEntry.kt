package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive proof of delivery entry records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a adaptive proof of delivery entry is in its life cycle. */
enum class AdaptiveProofOfDeliveryEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive proof of delivery entry. Its code is unique within a tenant. */
data class AdaptiveProofOfDeliveryEntry(
    var code: String,
    var title: String,
    var unit: String,
    var trailerId: String,
    var stopCount: Int,
    var shiftCode: String,
    var version: Int,
    var dock: String,
    var retries: Int,
    var status: AdaptiveProofOfDeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveProofOfDeliveryEntryStatus): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryEntryStatus.REQUESTED -> {
                when (next) {
                    AdaptiveProofOfDeliveryEntryStatus.GRANTED, AdaptiveProofOfDeliveryEntryStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryEntryStatus.GRANTED -> {
                when (next) {
                    AdaptiveProofOfDeliveryEntryStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryEntryStatus.REVOKED -> {
                when (next) {
                    AdaptiveProofOfDeliveryEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveProofOfDeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive proof of delivery entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((version.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveProofOfDeliveryEntry {
        return AdaptiveProofOfDeliveryEntry(
            code = code,
            title = title,
            unit = unit,
            trailerId = trailerId,
            stopCount = stopCount,
            shiftCode = shiftCode,
            version = version,
            dock = dock,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive proof of delivery entry in status requested. */
fun newAdaptiveProofOfDeliveryEntry(code: String, title: String): AdaptiveProofOfDeliveryEntry {
    return AdaptiveProofOfDeliveryEntry(
        code = code,
        title = title,
        unit = "",
        trailerId = "",
        stopCount = 0,
        shiftCode = "",
        version = 0,
        dock = "",
        retries = 0,
        status = AdaptiveProofOfDeliveryEntryStatus.REQUESTED,
    )
}
