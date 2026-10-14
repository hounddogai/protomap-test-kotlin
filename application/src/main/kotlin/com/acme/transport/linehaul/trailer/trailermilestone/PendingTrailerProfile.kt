package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending trailer profile is in its life cycle. */
enum class PendingTrailerProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending trailer profile as the trailer team keeps it. */
data class PendingTrailerProfile(
    var code: String,
    var name: String,
    var routeCode: String,
    var threshold: Double,
    var checksum: String,
    var createdAt: Int,
    var driverId: String,
    var phone: String,
    var lane: String,
    var expiresAt: Int,
    var startsAt: Int,
    var status: PendingTrailerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTrailerProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTrailerProfileStatus): Boolean {
        when (status) {
            PendingTrailerProfileStatus.PROPOSED -> {
                when (next) {
                    PendingTrailerProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTrailerProfileStatus.APPROVED -> {
                when (next) {
                    PendingTrailerProfileStatus.REJECTED, PendingTrailerProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTrailerProfileStatus.REJECTED -> {
                when (next) {
                    PendingTrailerProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTrailerProfileStatus) {
        if (!canMoveTo(next)) {
            error("pending trailer profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round(threshold * expiresAt.toDouble() * 100) / 100

    fun snapshot(): PendingTrailerProfile {
        return PendingTrailerProfile(
            code = code,
            name = name,
            routeCode = routeCode,
            threshold = threshold,
            checksum = checksum,
            createdAt = createdAt,
            driverId = driverId,
            phone = phone,
            lane = lane,
            expiresAt = expiresAt,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending trailer profile in status proposed. */
fun newPendingTrailerProfile(code: String, name: String): PendingTrailerProfile {
    return PendingTrailerProfile(
        code = code,
        name = name,
        routeCode = "",
        threshold = 0.0,
        checksum = "",
        createdAt = 0,
        driverId = "",
        phone = "",
        lane = "",
        expiresAt = 0,
        startsAt = 0,
        status = PendingTrailerProfileStatus.PROPOSED,
    )
}
