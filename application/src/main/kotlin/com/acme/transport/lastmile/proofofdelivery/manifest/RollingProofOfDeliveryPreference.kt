package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling proof of delivery preference is in its life cycle. */
enum class RollingProofOfDeliveryPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling proof of delivery preference as the proof of delivery team keeps it. */
data class RollingProofOfDeliveryPreference(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var stopCount: Int,
    var label: String,
    var ratio: Double,
    var carrier: String,
    var weight: Double,
    var minimum: Double,
    var status: RollingProofOfDeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryPreferenceStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryPreferenceStatus.PROPOSED -> {
                when (next) {
                    RollingProofOfDeliveryPreferenceStatus.APPROVED, RollingProofOfDeliveryPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingProofOfDeliveryPreferenceStatus.APPROVED -> return next == RollingProofOfDeliveryPreferenceStatus.REJECTED || next == RollingProofOfDeliveryPreferenceStatus.WITHDRAWN
            RollingProofOfDeliveryPreferenceStatus.REJECTED -> {
                when (next) {
                    RollingProofOfDeliveryPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun weightedValue(): Double = round((fuelLiters - stopCount.toDouble()) * 100) / 100

    fun snapshot(): RollingProofOfDeliveryPreference {
        return RollingProofOfDeliveryPreference(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            stopCount = stopCount,
            label = label,
            ratio = ratio,
            carrier = carrier,
            weight = weight,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling proof of delivery preference in status proposed. */
fun newRollingProofOfDeliveryPreference(code: String, name: String): RollingProofOfDeliveryPreference {
    return RollingProofOfDeliveryPreference(
        code = code,
        name = name,
        fuelLiters = 0.0,
        stopCount = 0,
        label = "",
        ratio = 0.0,
        carrier = "",
        weight = 0.0,
        minimum = 0.0,
        status = RollingProofOfDeliveryPreferenceStatus.PROPOSED,
    )
}
