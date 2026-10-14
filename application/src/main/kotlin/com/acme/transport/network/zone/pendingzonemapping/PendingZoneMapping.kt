package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending zone mapping records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a pending zone mapping is in its life cycle. */
enum class PendingZoneMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending zone mapping as the zone team keeps it. */
data class PendingZoneMapping(
    var code: String,
    var label: String,
    var trailerId: String,
    var updatedAt: Int,
    var startsAt: Int,
    var loadKg: Double,
    var status: PendingZoneMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingZoneMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingZoneMappingStatus): Boolean {
        when (status) {
            PendingZoneMappingStatus.CREATED -> return next == PendingZoneMappingStatus.RETIRED
            PendingZoneMappingStatus.VALIDATED -> {
                when (next) {
                    PendingZoneMappingStatus.PUBLISHED, PendingZoneMappingStatus.RETIRED -> return true
                }
                return false
            }
            PendingZoneMappingStatus.PUBLISHED -> {
                when (next) {
                    PendingZoneMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingZoneMappingStatus) {
        if (!canMoveTo(next)) {
            error("pending zone mapping $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((startsAt.toDouble() + loadKg) * 100) / 100

    fun snapshot(): PendingZoneMapping {
        return PendingZoneMapping(
            code = code,
            label = label,
            trailerId = trailerId,
            updatedAt = updatedAt,
            startsAt = startsAt,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending zone mapping in status created. */
fun newPendingZoneMapping(code: String, label: String): PendingZoneMapping {
    return PendingZoneMapping(
        code = code,
        label = label,
        trailerId = "",
        updatedAt = 0,
        startsAt = 0,
        loadKg = 0.0,
        status = PendingZoneMappingStatus.CREATED,
    )
}
