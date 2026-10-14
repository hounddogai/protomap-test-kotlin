package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary transit time bundle is in its life cycle. */
enum class PrimaryTransitTimeBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One primary transit time bundle, with what the planners need to act on it. */
data class PrimaryTransitTimeBundle(
    var code: String,
    var name: String,
    var floorValue: Double,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var status: PrimaryTransitTimeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeBundleStatus): Boolean {
        when (status) {
            PrimaryTransitTimeBundleStatus.PLANNED -> {
                when (next) {
                    PrimaryTransitTimeBundleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PrimaryTransitTimeBundleStatus.SCHEDULED -> return next == PrimaryTransitTimeBundleStatus.IN_PROGRESS || next == PrimaryTransitTimeBundleStatus.COMPLETED
            PrimaryTransitTimeBundleStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryTransitTimeBundleStatus.COMPLETED, PrimaryTransitTimeBundleStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryTransitTimeBundleStatus.COMPLETED -> {
                when (next) {
                    PrimaryTransitTimeBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(floorValue * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): PrimaryTransitTimeBundle {
        return PrimaryTransitTimeBundle(
            code = code,
            name = name,
            floorValue = floorValue,
            routeCode = routeCode,
            attributes = attributes,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary transit time bundle in status planned. */
fun newPrimaryTransitTimeBundle(code: String, name: String): PrimaryTransitTimeBundle {
    return PrimaryTransitTimeBundle(
        code = code,
        name = name,
        floorValue = 0.0,
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        status = PrimaryTransitTimeBundleStatus.PLANNED,
    )
}
