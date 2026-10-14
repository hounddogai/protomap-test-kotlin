package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock profile is in its life cycle. */
enum class CrossDockProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One cross dock profile, with what the planners need to act on it. */
data class CrossDockProfile(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var distanceKm: Double,
    var fuelLiters: Double,
    var country: String,
    var graceDays: Int,
    var externalRef: String,
    var status: CrossDockProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockProfileStatus): Boolean {
        when (status) {
            CrossDockProfileStatus.PROPOSED -> return next == CrossDockProfileStatus.APPROVED || next == CrossDockProfileStatus.REJECTED
            CrossDockProfileStatus.APPROVED -> {
                when (next) {
                    CrossDockProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            CrossDockProfileStatus.REJECTED -> return next == CrossDockProfileStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CrossDockProfileStatus) {
        if (!canMoveTo(next)) {
            error("cross dock profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((graceDays.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): CrossDockProfile {
        return CrossDockProfile(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            fuelLiters = fuelLiters,
            country = country,
            graceDays = graceDays,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a cross dock profile in status proposed. */
fun newCrossDockProfile(code: String, title: String): CrossDockProfile {
    return CrossDockProfile(
        code = code,
        title = title,
        durationMinutes = 0,
        distanceKm = 0.0,
        fuelLiters = 0.0,
        country = "",
        graceDays = 0,
        externalRef = "",
        status = CrossDockProfileStatus.PROPOSED,
    )
}
