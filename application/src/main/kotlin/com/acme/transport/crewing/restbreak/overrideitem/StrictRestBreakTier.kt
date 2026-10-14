package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rest break tier is in its life cycle. */
enum class StrictRestBreakTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict rest break tier, with what the planners need to act on it. */
data class StrictRestBreakTier(
    var code: String,
    var title: String,
    var ratio: Double,
    var minimum: Double,
    var unit: String,
    var loadKg: Double,
    var status: StrictRestBreakTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRestBreakTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRestBreakTierStatus): Boolean {
        when (status) {
            StrictRestBreakTierStatus.OPEN -> {
                when (next) {
                    StrictRestBreakTierStatus.RESOLVED, StrictRestBreakTierStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictRestBreakTierStatus.IN_REVIEW -> return next == StrictRestBreakTierStatus.REOPENED || next == StrictRestBreakTierStatus.ARCHIVED
            StrictRestBreakTierStatus.RESOLVED -> return next == StrictRestBreakTierStatus.ARCHIVED
            StrictRestBreakTierStatus.REOPENED -> return next == StrictRestBreakTierStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictRestBreakTierStatus) {
        if (!canMoveTo(next)) {
            error("strict rest break tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict rest break tier"} $code"

    fun effectiveExposure(): Double = round((minimum + loadKg) * 100) / 100

    fun snapshot(): StrictRestBreakTier {
        return StrictRestBreakTier(
            code = code,
            title = title,
            ratio = ratio,
            minimum = minimum,
            unit = unit,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict rest break tier in status open. */
fun newStrictRestBreakTier(code: String, title: String): StrictRestBreakTier {
    return StrictRestBreakTier(
        code = code,
        title = title,
        ratio = 0.0,
        minimum = 0.0,
        unit = "",
        loadKg = 0.0,
        status = StrictRestBreakTierStatus.OPEN,
    )
}
