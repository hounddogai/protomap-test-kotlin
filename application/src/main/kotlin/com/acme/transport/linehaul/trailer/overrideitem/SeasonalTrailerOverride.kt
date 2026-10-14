package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal trailer override records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a seasonal trailer override is in its life cycle. */
enum class SeasonalTrailerOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal trailer override as the trailer team keeps it. */
data class SeasonalTrailerOverride(
    var code: String,
    var label: String,
    var retries: Int,
    var durationMinutes: Int,
    var quantity: Int,
    var unit: String,
    var visible: Boolean,
    var externalRef: String,
    var status: SeasonalTrailerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerOverrideStatus): Boolean {
        when (status) {
            SeasonalTrailerOverrideStatus.OPEN -> {
                when (next) {
                    SeasonalTrailerOverrideStatus.IN_REVIEW, SeasonalTrailerOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalTrailerOverrideStatus.IN_REVIEW -> return next == SeasonalTrailerOverrideStatus.ARCHIVED
            SeasonalTrailerOverrideStatus.RESOLVED -> return next == SeasonalTrailerOverrideStatus.REOPENED || next == SeasonalTrailerOverrideStatus.ARCHIVED
            SeasonalTrailerOverrideStatus.REOPENED -> {
                when (next) {
                    SeasonalTrailerOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(durationMinutes.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): SeasonalTrailerOverride {
        return SeasonalTrailerOverride(
            code = code,
            label = label,
            retries = retries,
            durationMinutes = durationMinutes,
            quantity = quantity,
            unit = unit,
            visible = visible,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal trailer override in status open. */
fun newSeasonalTrailerOverride(code: String, label: String): SeasonalTrailerOverride {
    return SeasonalTrailerOverride(
        code = code,
        label = label,
        retries = 0,
        durationMinutes = 0,
        quantity = 0,
        unit = "",
        visible = false,
        externalRef = "",
        status = SeasonalTrailerOverrideStatus.OPEN,
    )
}
