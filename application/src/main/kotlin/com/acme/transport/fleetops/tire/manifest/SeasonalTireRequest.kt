package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal tire request is in its life cycle. */
enum class SeasonalTireRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal tire request as the tire team keeps it. */
data class SeasonalTireRequest(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var summary: String,
    var distanceKm: Double,
    var channel: String,
    var region: String,
    var updatedAt: Int,
    var checksum: String,
    var status: SeasonalTireRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTireRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTireRequestStatus): Boolean {
        when (status) {
            SeasonalTireRequestStatus.IDLE -> {
                when (next) {
                    SeasonalTireRequestStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalTireRequestStatus.WARMING -> return next == SeasonalTireRequestStatus.DRAINING
            SeasonalTireRequestStatus.READY -> {
                when (next) {
                    SeasonalTireRequestStatus.DRAINING, SeasonalTireRequestStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalTireRequestStatus.DRAINING -> {
                when (next) {
                    SeasonalTireRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTireRequestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tire request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round(distanceKm * updatedAt.toDouble() * 100) / 100

    fun snapshot(): SeasonalTireRequest {
        return SeasonalTireRequest(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            summary = summary,
            distanceKm = distanceKm,
            channel = channel,
            region = region,
            updatedAt = updatedAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal tire request in status idle. */
fun newSeasonalTireRequest(code: String, name: String): SeasonalTireRequest {
    return SeasonalTireRequest(
        code = code,
        name = name,
        durationMinutes = 0,
        summary = "",
        distanceKm = 0.0,
        channel = "",
        region = "",
        updatedAt = 0,
        checksum = "",
        status = SeasonalTireRequestStatus.IDLE,
    )
}
