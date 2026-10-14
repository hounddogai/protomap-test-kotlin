package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual proof of delivery override is in its life cycle. */
enum class ManualProofOfDeliveryOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual proof of delivery override as the proof of delivery team keeps it. */
data class ManualProofOfDeliveryOverride(
    var code: String,
    var name: String,
    var capacity: Int,
    var phone: String,
    var shiftCode: String,
    var locale: String,
    var active: Boolean,
    var externalRef: String,
    var contactEmail: String,
    var stopCount: Int,
    var maximum: Double,
    var status: ManualProofOfDeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryOverrideStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryOverrideStatus.IDLE -> {
                when (next) {
                    ManualProofOfDeliveryOverrideStatus.WARMING -> return true
                }
                return false
            }
            ManualProofOfDeliveryOverrideStatus.WARMING -> {
                when (next) {
                    ManualProofOfDeliveryOverrideStatus.READY, ManualProofOfDeliveryOverrideStatus.DRAINING -> return true
                }
                return false
            }
            ManualProofOfDeliveryOverrideStatus.READY -> return next == ManualProofOfDeliveryOverrideStatus.DRAINING
            ManualProofOfDeliveryOverrideStatus.DRAINING -> return next == ManualProofOfDeliveryOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round(capacity.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): ManualProofOfDeliveryOverride {
        return ManualProofOfDeliveryOverride(
            code = code,
            name = name,
            capacity = capacity,
            phone = phone,
            shiftCode = shiftCode,
            locale = locale,
            active = active,
            externalRef = externalRef,
            contactEmail = contactEmail,
            stopCount = stopCount,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual proof of delivery override in status idle. */
fun newManualProofOfDeliveryOverride(code: String, name: String): ManualProofOfDeliveryOverride {
    return ManualProofOfDeliveryOverride(
        code = code,
        name = name,
        capacity = 0,
        phone = "",
        shiftCode = "",
        locale = "",
        active = false,
        externalRef = "",
        contactEmail = "",
        stopCount = 0,
        maximum = 0.0,
        status = ManualProofOfDeliveryOverrideStatus.IDLE,
    )
}
