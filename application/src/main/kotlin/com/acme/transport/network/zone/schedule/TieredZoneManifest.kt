package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered zone manifest is in its life cycle. */
enum class TieredZoneManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered zone manifest as the zone team keeps it. */
data class TieredZoneManifest(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var contactEmail: String,
    var sequence: Int,
    var batchSize: Int,
    var driverId: String,
    var notes: String,
    var retries: Int,
    var etaMinutes: Int,
    var status: TieredZoneManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredZoneManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredZoneManifestStatus): Boolean {
        when (status) {
            TieredZoneManifestStatus.PROPOSED -> {
                when (next) {
                    TieredZoneManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredZoneManifestStatus.APPROVED -> return next == TieredZoneManifestStatus.REJECTED
            TieredZoneManifestStatus.REJECTED -> {
                when (next) {
                    TieredZoneManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredZoneManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered zone manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((distanceKm + batchSize.toDouble()) * 100) / 100

    fun snapshot(): TieredZoneManifest {
        return TieredZoneManifest(
            code = code,
            label = label,
            tags = tags,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            sequence = sequence,
            batchSize = batchSize,
            driverId = driverId,
            notes = notes,
            retries = retries,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered zone manifest in status proposed. */
fun newTieredZoneManifest(code: String, label: String): TieredZoneManifest {
    return TieredZoneManifest(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        contactEmail = "",
        sequence = 0,
        batchSize = 0,
        driverId = "",
        notes = "",
        retries = 0,
        etaMinutes = 0,
        status = TieredZoneManifestStatus.PROPOSED,
    )
}
