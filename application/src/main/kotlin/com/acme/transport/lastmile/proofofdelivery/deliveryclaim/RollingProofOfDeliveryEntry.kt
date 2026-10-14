package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling proof of delivery entry is in its life cycle. */
enum class RollingProofOfDeliveryEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling proof of delivery entry and its current status. */
data class RollingProofOfDeliveryEntry(
    var code: String,
    var title: String,
    var discount: Double,
    var shiftCode: String,
    var pallets: Int,
    var externalRef: String,
    var priority: Int,
    var carrier: String,
    var status: RollingProofOfDeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryEntryStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryEntryStatus.OPEN -> {
                when (next) {
                    RollingProofOfDeliveryEntryStatus.IN_REVIEW, RollingProofOfDeliveryEntryStatus.REOPENED -> return true
                }
                return false
            }
            RollingProofOfDeliveryEntryStatus.IN_REVIEW -> {
                when (next) {
                    RollingProofOfDeliveryEntryStatus.RESOLVED, RollingProofOfDeliveryEntryStatus.REOPENED -> return true
                }
                return false
            }
            RollingProofOfDeliveryEntryStatus.RESOLVED -> {
                when (next) {
                    RollingProofOfDeliveryEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingProofOfDeliveryEntryStatus.REOPENED -> {
                when (next) {
                    RollingProofOfDeliveryEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling proof of delivery entry"} $code"

    fun projectedExposure(): Double = round((priority.toDouble() - discount) * 100) / 100

    fun snapshot(): RollingProofOfDeliveryEntry {
        return RollingProofOfDeliveryEntry(
            code = code,
            title = title,
            discount = discount,
            shiftCode = shiftCode,
            pallets = pallets,
            externalRef = externalRef,
            priority = priority,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling proof of delivery entry in status open. */
fun newRollingProofOfDeliveryEntry(code: String, title: String): RollingProofOfDeliveryEntry {
    return RollingProofOfDeliveryEntry(
        code = code,
        title = title,
        discount = 0.0,
        shiftCode = "",
        pallets = 0,
        externalRef = "",
        priority = 0,
        carrier = "",
        status = RollingProofOfDeliveryEntryStatus.OPEN,
    )
}
