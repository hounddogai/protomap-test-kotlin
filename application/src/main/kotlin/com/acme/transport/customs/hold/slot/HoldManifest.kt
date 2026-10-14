package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold manifest is in its life cycle. */
enum class HoldManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A hold manifest as the hold team keeps it. */
data class HoldManifest(
    var code: String,
    var name: String,
    var reviewer: String,
    var ownerId: String,
    var shiftCode: String,
    var floorValue: Double,
    var weight: Double,
    var retries: Int,
    var fuelLiters: Double,
    var channel: String,
    var status: HoldManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldManifestStatus): Boolean {
        when (status) {
            HoldManifestStatus.PLANNED -> return next == HoldManifestStatus.COMPLETED
            HoldManifestStatus.SCHEDULED -> return next == HoldManifestStatus.COMPLETED || next == HoldManifestStatus.CANCELLED
            HoldManifestStatus.IN_PROGRESS -> {
                when (next) {
                    HoldManifestStatus.CANCELLED -> return true
                }
                return false
            }
            HoldManifestStatus.COMPLETED -> {
                when (next) {
                    HoldManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldManifestStatus) {
        if (!canMoveTo(next)) {
            error("hold manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((retries.toDouble() - weight) * 100) / 100

    fun snapshot(): HoldManifest {
        return HoldManifest(
            code = code,
            name = name,
            reviewer = reviewer,
            ownerId = ownerId,
            shiftCode = shiftCode,
            floorValue = floorValue,
            weight = weight,
            retries = retries,
            fuelLiters = fuelLiters,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a hold manifest in status planned. */
fun newHoldManifest(code: String, name: String): HoldManifest {
    return HoldManifest(
        code = code,
        name = name,
        reviewer = "",
        ownerId = "",
        shiftCode = "",
        floorValue = 0.0,
        weight = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        channel = "",
        status = HoldManifestStatus.PLANNED,
    )
}
