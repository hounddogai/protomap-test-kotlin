package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy zone override records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a legacy zone override is in its life cycle. */
enum class LegacyZoneOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy zone override as the zone team keeps it. */
data class LegacyZoneOverride(
    var code: String,
    var title: String,
    var version: Int,
    var attempts: Int,
    var shiftCode: String,
    var ratio: Double,
    var dock: String,
    var approved: Boolean,
    var trailerId: String,
    var reviewer: String,
    var ownerId: String,
    var status: LegacyZoneOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneOverrideStatus): Boolean {
        when (status) {
            LegacyZoneOverrideStatus.REQUESTED -> {
                when (next) {
                    LegacyZoneOverrideStatus.GRANTED, LegacyZoneOverrideStatus.REVOKED -> return true
                }
                return false
            }
            LegacyZoneOverrideStatus.GRANTED -> return next == LegacyZoneOverrideStatus.REVOKED || next == LegacyZoneOverrideStatus.EXPIRED
            LegacyZoneOverrideStatus.REVOKED -> {
                when (next) {
                    LegacyZoneOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyZoneOverrideStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone override $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"legacy zone override"} $code"

    fun effectiveLoad(): Double = round((attempts.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): LegacyZoneOverride {
        return LegacyZoneOverride(
            code = code,
            title = title,
            version = version,
            attempts = attempts,
            shiftCode = shiftCode,
            ratio = ratio,
            dock = dock,
            approved = approved,
            trailerId = trailerId,
            reviewer = reviewer,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy zone override in status requested. */
fun newLegacyZoneOverride(code: String, title: String): LegacyZoneOverride {
    return LegacyZoneOverride(
        code = code,
        title = title,
        version = 0,
        attempts = 0,
        shiftCode = "",
        ratio = 0.0,
        dock = "",
        approved = false,
        trailerId = "",
        reviewer = "",
        ownerId = "",
        status = LegacyZoneOverrideStatus.REQUESTED,
    )
}
