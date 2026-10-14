package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker preference records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a locker preference is in its life cycle. */
enum class LockerPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A locker preference as the locker team keeps it. */
data class LockerPreference(
    var code: String,
    var name: String,
    var lane: String,
    var durationMinutes: Int,
    var windowDays: Int,
    var region: String,
    var ceiling: Double,
    var reviewer: String,
    var status: LockerPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerPreferenceStatus): Boolean {
        when (status) {
            LockerPreferenceStatus.REQUESTED -> {
                when (next) {
                    LockerPreferenceStatus.REVOKED, LockerPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            LockerPreferenceStatus.GRANTED -> {
                when (next) {
                    LockerPreferenceStatus.REVOKED, LockerPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            LockerPreferenceStatus.REVOKED -> {
                when (next) {
                    LockerPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("locker preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((windowDays.toDouble() + ceiling) * 100) / 100

    fun snapshot(): LockerPreference {
        return LockerPreference(
            code = code,
            name = name,
            lane = lane,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            region = region,
            ceiling = ceiling,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker preference in status requested. */
fun newLockerPreference(code: String, name: String): LockerPreference {
    return LockerPreference(
        code = code,
        name = name,
        lane = "",
        durationMinutes = 0,
        windowDays = 0,
        region = "",
        ceiling = 0.0,
        reviewer = "",
        status = LockerPreferenceStatus.REQUESTED,
    )
}
