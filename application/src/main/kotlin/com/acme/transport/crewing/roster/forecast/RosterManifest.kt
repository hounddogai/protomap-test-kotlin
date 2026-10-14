package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster manifest is in its life cycle. */
enum class RosterManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a roster manifest and its current status. */
data class RosterManifest(
    var code: String,
    var title: String,
    var ratio: Double,
    var endsAt: Int,
    var odometer: Int,
    var maximum: Double,
    var status: RosterManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterManifestStatus): Boolean {
        when (status) {
            RosterManifestStatus.REQUESTED -> return next == RosterManifestStatus.GRANTED
            RosterManifestStatus.GRANTED -> return next == RosterManifestStatus.REVOKED || next == RosterManifestStatus.EXPIRED
            RosterManifestStatus.REVOKED -> {
                when (next) {
                    RosterManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterManifestStatus) {
        if (!canMoveTo(next)) {
            error("roster manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster manifest"} $code"

    fun netVolume(): Double = round(ratio * odometer.toDouble() * 100) / 100

    fun snapshot(): RosterManifest {
        return RosterManifest(
            code = code,
            title = title,
            ratio = ratio,
            endsAt = endsAt,
            odometer = odometer,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster manifest in status requested. */
fun newRosterManifest(code: String, title: String): RosterManifest {
    return RosterManifest(
        code = code,
        title = title,
        ratio = 0.0,
        endsAt = 0,
        odometer = 0,
        maximum = 0.0,
        status = RosterManifestStatus.REQUESTED,
    )
}
