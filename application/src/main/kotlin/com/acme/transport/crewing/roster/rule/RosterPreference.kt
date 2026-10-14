package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster preference is in its life cycle. */
enum class RosterPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One roster preference, with what the planners need to act on it. */
data class RosterPreference(
    var code: String,
    var title: String,
    var attempts: Int,
    var weight: Double,
    var expiresAt: Int,
    var odometer: Int,
    var loadKg: Double,
    var stopCount: Int,
    var description: String,
    var status: RosterPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterPreferenceStatus): Boolean {
        when (status) {
            RosterPreferenceStatus.CREATED -> return next == RosterPreferenceStatus.VALIDATED
            RosterPreferenceStatus.VALIDATED -> {
                when (next) {
                    RosterPreferenceStatus.PUBLISHED, RosterPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            RosterPreferenceStatus.PUBLISHED -> return next == RosterPreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("roster preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round((odometer.toDouble() - weight) * 100) / 100

    fun snapshot(): RosterPreference {
        return RosterPreference(
            code = code,
            title = title,
            attempts = attempts,
            weight = weight,
            expiresAt = expiresAt,
            odometer = odometer,
            loadKg = loadKg,
            stopCount = stopCount,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster preference in status created. */
fun newRosterPreference(code: String, title: String): RosterPreference {
    return RosterPreference(
        code = code,
        title = title,
        attempts = 0,
        weight = 0.0,
        expiresAt = 0,
        odometer = 0,
        loadKg = 0.0,
        stopCount = 0,
        description = "",
        status = RosterPreferenceStatus.CREATED,
    )
}
