package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dock door score is in its life cycle. */
enum class SeasonalDockDoorScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal dock door score. Its code is unique within a tenant. */
data class SeasonalDockDoorScore(
    var code: String,
    var name: String,
    var summary: String,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var distanceKm: Double,
    var contactEmail: String,
    var driverId: String,
    var status: SeasonalDockDoorScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDockDoorScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDockDoorScoreStatus): Boolean {
        when (status) {
            SeasonalDockDoorScoreStatus.REQUESTED -> return next == SeasonalDockDoorScoreStatus.REVOKED
            SeasonalDockDoorScoreStatus.GRANTED -> return next == SeasonalDockDoorScoreStatus.REVOKED || next == SeasonalDockDoorScoreStatus.EXPIRED
            SeasonalDockDoorScoreStatus.REVOKED -> return next == SeasonalDockDoorScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalDockDoorScoreStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dock door score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(distanceKm * createdAt.toDouble() * 100) / 100

    fun snapshot(): SeasonalDockDoorScore {
        return SeasonalDockDoorScore(
            code = code,
            name = name,
            summary = summary,
            createdAt = createdAt,
            attributes = attributes,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal dock door score in status requested. */
fun newSeasonalDockDoorScore(code: String, name: String): SeasonalDockDoorScore {
    return SeasonalDockDoorScore(
        code = code,
        name = name,
        summary = "",
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        distanceKm = 0.0,
        contactEmail = "",
        driverId = "",
        status = SeasonalDockDoorScoreStatus.REQUESTED,
    )
}
