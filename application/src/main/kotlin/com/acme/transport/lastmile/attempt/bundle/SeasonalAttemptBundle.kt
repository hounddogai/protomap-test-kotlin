package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal attempt bundle records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a seasonal attempt bundle is in its life cycle. */
enum class SeasonalAttemptBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal attempt bundle as the attempt team keeps it. */
data class SeasonalAttemptBundle(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var capacity: Int,
    var externalRef: String,
    var etaMinutes: Int,
    var source: String,
    var vehicleId: String,
    var status: SeasonalAttemptBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAttemptBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAttemptBundleStatus): Boolean {
        when (status) {
            SeasonalAttemptBundleStatus.CREATED -> {
                when (next) {
                    SeasonalAttemptBundleStatus.VALIDATED -> return true
                }
                return false
            }
            SeasonalAttemptBundleStatus.VALIDATED -> return next == SeasonalAttemptBundleStatus.PUBLISHED
            SeasonalAttemptBundleStatus.PUBLISHED -> return next == SeasonalAttemptBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalAttemptBundleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal attempt bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"seasonal attempt bundle"} $code"

    fun netExposure(): Double = round(etaMinutes.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): SeasonalAttemptBundle {
        return SeasonalAttemptBundle(
            code = code,
            label = label,
            expiresAt = expiresAt,
            capacity = capacity,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            source = source,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal attempt bundle in status created. */
fun newSeasonalAttemptBundle(code: String, label: String): SeasonalAttemptBundle {
    return SeasonalAttemptBundle(
        code = code,
        label = label,
        expiresAt = 0,
        capacity = 0,
        externalRef = "",
        etaMinutes = 0,
        source = "",
        vehicleId = "",
        status = SeasonalAttemptBundleStatus.CREATED,
    )
}
