package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire manifest records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire manifest is in its life cycle. */
enum class TireManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tire manifest and its current status. */
data class TireManifest(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var unit: String,
    var odometer: Int,
    var endsAt: Int,
    var channel: String,
    var title: String,
    var pinned: Boolean,
    var loadKg: Double,
    var status: TireManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireManifestStatus): Boolean {
        when (status) {
            TireManifestStatus.PROPOSED -> {
                when (next) {
                    TireManifestStatus.APPROVED, TireManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            TireManifestStatus.APPROVED -> return next == TireManifestStatus.REJECTED
            TireManifestStatus.REJECTED -> {
                when (next) {
                    TireManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireManifestStatus) {
        if (!canMoveTo(next)) {
            error("tire manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tire manifest"} $code"

    fun adjustedValue(): Double = round((endsAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): TireManifest {
        return TireManifest(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            unit = unit,
            odometer = odometer,
            endsAt = endsAt,
            channel = channel,
            title = title,
            pinned = pinned,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tire manifest in status proposed. */
fun newTireManifest(code: String, name: String): TireManifest {
    return TireManifest(
        code = code,
        name = name,
        fuelLiters = 0.0,
        etaMinutes = 0,
        unit = "",
        odometer = 0,
        endsAt = 0,
        channel = "",
        title = "",
        pinned = false,
        loadKg = 0.0,
        status = TireManifestStatus.PROPOSED,
    )
}
