package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dock door group is in its life cycle. */
enum class FallbackDockDoorGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One fallback dock door group, with what the planners need to act on it. */
data class FallbackDockDoorGroup(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var retries: Int,
    var unit: String,
    var status: FallbackDockDoorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDockDoorGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDockDoorGroupStatus): Boolean {
        when (status) {
            FallbackDockDoorGroupStatus.CREATED -> {
                when (next) {
                    FallbackDockDoorGroupStatus.VALIDATED, FallbackDockDoorGroupStatus.RETIRED -> return true
                }
                return false
            }
            FallbackDockDoorGroupStatus.VALIDATED -> {
                when (next) {
                    FallbackDockDoorGroupStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackDockDoorGroupStatus.PUBLISHED -> return next == FallbackDockDoorGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackDockDoorGroupStatus) {
        if (!canMoveTo(next)) {
            error("fallback dock door group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round(distanceKm * retries.toDouble() * 100) / 100

    fun snapshot(): FallbackDockDoorGroup {
        return FallbackDockDoorGroup(
            code = code,
            title = title,
            distanceKm = distanceKm,
            retries = retries,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback dock door group in status created. */
fun newFallbackDockDoorGroup(code: String, title: String): FallbackDockDoorGroup {
    return FallbackDockDoorGroup(
        code = code,
        title = title,
        distanceKm = 0.0,
        retries = 0,
        unit = "",
        status = FallbackDockDoorGroupStatus.CREATED,
    )
}
