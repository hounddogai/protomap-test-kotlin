package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tire digest is in its life cycle. */
enum class LegacyTireDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy tire digest and its current status. */
data class LegacyTireDigest(
    var code: String,
    var label: String,
    var rate: Double,
    var description: String,
    var distanceKm: Double,
    var driverId: String,
    var status: LegacyTireDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireDigestStatus): Boolean {
        when (status) {
            LegacyTireDigestStatus.OPEN -> return next == LegacyTireDigestStatus.REOPENED
            LegacyTireDigestStatus.IN_REVIEW -> {
                when (next) {
                    LegacyTireDigestStatus.RESOLVED, LegacyTireDigestStatus.REOPENED -> return true
                }
                return false
            }
            LegacyTireDigestStatus.RESOLVED -> return next == LegacyTireDigestStatus.REOPENED
            LegacyTireDigestStatus.REOPENED -> return next == LegacyTireDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyTireDigestStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy tire digest"} $code"

    fun weightedValue(): Double = round((distanceKm + rate) * 100) / 100

    fun snapshot(): LegacyTireDigest {
        return LegacyTireDigest(
            code = code,
            label = label,
            rate = rate,
            description = description,
            distanceKm = distanceKm,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy tire digest in status open. */
fun newLegacyTireDigest(code: String, label: String): LegacyTireDigest {
    return LegacyTireDigest(
        code = code,
        label = label,
        rate = 0.0,
        description = "",
        distanceKm = 0.0,
        driverId = "",
        status = LegacyTireDigestStatus.OPEN,
    )
}
