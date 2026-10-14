package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional service area manifest records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a regional service area manifest is in its life cycle. */
enum class RegionalServiceAreaManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional service area manifest and its current status. */
data class RegionalServiceAreaManifest(
    var code: String,
    var name: String,
    var startsAt: Int,
    var expiresAt: Int,
    var fuelLiters: Double,
    var pinned: Boolean,
    var timezone: String,
    var status: RegionalServiceAreaManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaManifestStatus): Boolean {
        when (status) {
            RegionalServiceAreaManifestStatus.PLANNED -> return next == RegionalServiceAreaManifestStatus.COMPLETED || next == RegionalServiceAreaManifestStatus.CANCELLED
            RegionalServiceAreaManifestStatus.SCHEDULED -> return next == RegionalServiceAreaManifestStatus.COMPLETED || next == RegionalServiceAreaManifestStatus.CANCELLED
            RegionalServiceAreaManifestStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalServiceAreaManifestStatus.COMPLETED, RegionalServiceAreaManifestStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalServiceAreaManifestStatus.COMPLETED -> {
                when (next) {
                    RegionalServiceAreaManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional service area manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((expiresAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalServiceAreaManifest {
        return RegionalServiceAreaManifest(
            code = code,
            name = name,
            startsAt = startsAt,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            pinned = pinned,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional service area manifest in status planned. */
fun newRegionalServiceAreaManifest(code: String, name: String): RegionalServiceAreaManifest {
    return RegionalServiceAreaManifest(
        code = code,
        name = name,
        startsAt = 0,
        expiresAt = 0,
        fuelLiters = 0.0,
        pinned = false,
        timezone = "",
        status = RegionalServiceAreaManifestStatus.PLANNED,
    )
}
