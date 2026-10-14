package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy zone manifest is in its life cycle. */
enum class LegacyZoneManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy zone manifest, with what the planners need to act on it. */
data class LegacyZoneManifest(
    var code: String,
    var label: String,
    var active: Boolean,
    var sequence: Int,
    var fuelLiters: Double,
    var weight: Double,
    var createdAt: Int,
    var etaMinutes: Int,
    var cost: Double,
    var name: String,
    var externalRef: String,
    var status: LegacyZoneManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneManifestStatus): Boolean {
        when (status) {
            LegacyZoneManifestStatus.OPEN -> {
                when (next) {
                    LegacyZoneManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyZoneManifestStatus.IN_REVIEW -> {
                when (next) {
                    LegacyZoneManifestStatus.RESOLVED, LegacyZoneManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyZoneManifestStatus.RESOLVED -> return next == LegacyZoneManifestStatus.ARCHIVED
            LegacyZoneManifestStatus.REOPENED -> {
                when (next) {
                    LegacyZoneManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyZoneManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round((etaMinutes.toDouble() + weight) * 100) / 100

    fun snapshot(): LegacyZoneManifest {
        return LegacyZoneManifest(
            code = code,
            label = label,
            active = active,
            sequence = sequence,
            fuelLiters = fuelLiters,
            weight = weight,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            cost = cost,
            name = name,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy zone manifest in status open. */
fun newLegacyZoneManifest(code: String, label: String): LegacyZoneManifest {
    return LegacyZoneManifest(
        code = code,
        label = label,
        active = false,
        sequence = 0,
        fuelLiters = 0.0,
        weight = 0.0,
        createdAt = 0,
        etaMinutes = 0,
        cost = 0.0,
        name = "",
        externalRef = "",
        status = LegacyZoneManifestStatus.OPEN,
    )
}
