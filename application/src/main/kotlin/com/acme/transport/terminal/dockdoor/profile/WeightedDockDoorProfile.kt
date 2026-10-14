package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted dock door profile records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a weighted dock door profile is in its life cycle. */
enum class WeightedDockDoorProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One weighted dock door profile, with what the planners need to act on it. */
data class WeightedDockDoorProfile(
    var code: String,
    var title: String,
    var routeCode: String,
    var capacity: Int,
    var pallets: Int,
    var summary: String,
    var locale: String,
    var ratio: Double,
    var distanceKm: Double,
    var startsAt: Int,
    var status: WeightedDockDoorProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDockDoorProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDockDoorProfileStatus): Boolean {
        when (status) {
            WeightedDockDoorProfileStatus.DRAFT -> return next == WeightedDockDoorProfileStatus.ACTIVE
            WeightedDockDoorProfileStatus.PENDING -> {
                when (next) {
                    WeightedDockDoorProfileStatus.ACTIVE, WeightedDockDoorProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedDockDoorProfileStatus.ACTIVE -> return next == WeightedDockDoorProfileStatus.SUSPENDED || next == WeightedDockDoorProfileStatus.CLOSED
            WeightedDockDoorProfileStatus.SUSPENDED -> {
                when (next) {
                    WeightedDockDoorProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDockDoorProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted dock door profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((distanceKm + startsAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedDockDoorProfile {
        return WeightedDockDoorProfile(
            code = code,
            title = title,
            routeCode = routeCode,
            capacity = capacity,
            pallets = pallets,
            summary = summary,
            locale = locale,
            ratio = ratio,
            distanceKm = distanceKm,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted dock door profile in status draft. */
fun newWeightedDockDoorProfile(code: String, title: String): WeightedDockDoorProfile {
    return WeightedDockDoorProfile(
        code = code,
        title = title,
        routeCode = "",
        capacity = 0,
        pallets = 0,
        summary = "",
        locale = "",
        ratio = 0.0,
        distanceKm = 0.0,
        startsAt = 0,
        status = WeightedDockDoorProfileStatus.DRAFT,
    )
}
