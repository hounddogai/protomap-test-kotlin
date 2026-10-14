package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge manifest is in its life cycle. */
enum class FuelSurchargeManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel surcharge manifest, with what the planners need to act on it. */
data class FuelSurchargeManifest(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var odometer: Int,
    var stopCount: Int,
    var weight: Double,
    var ratio: Double,
    var rate: Double,
    var vehicleId: String,
    var status: FuelSurchargeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeManifestStatus): Boolean {
        when (status) {
            FuelSurchargeManifestStatus.IDLE -> return next == FuelSurchargeManifestStatus.STOPPED
            FuelSurchargeManifestStatus.WARMING -> {
                when (next) {
                    FuelSurchargeManifestStatus.DRAINING -> return true
                }
                return false
            }
            FuelSurchargeManifestStatus.READY -> return next == FuelSurchargeManifestStatus.DRAINING || next == FuelSurchargeManifestStatus.STOPPED
            FuelSurchargeManifestStatus.DRAINING -> return next == FuelSurchargeManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeManifestStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fuel surcharge manifest"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round(rate * odometer.toDouble() * 100) / 100

    fun snapshot(): FuelSurchargeManifest {
        return FuelSurchargeManifest(
            code = code,
            label = label,
            attributes = attributes,
            odometer = odometer,
            stopCount = stopCount,
            weight = weight,
            ratio = ratio,
            rate = rate,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge manifest in status idle. */
fun newFuelSurchargeManifest(code: String, label: String): FuelSurchargeManifest {
    return FuelSurchargeManifest(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        odometer = 0,
        stopCount = 0,
        weight = 0.0,
        ratio = 0.0,
        rate = 0.0,
        vehicleId = "",
        status = FuelSurchargeManifestStatus.IDLE,
    )
}
