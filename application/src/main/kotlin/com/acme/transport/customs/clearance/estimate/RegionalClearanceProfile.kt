package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional clearance profile is in its life cycle. */
enum class RegionalClearanceProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional clearance profile, with what the planners need to act on it. */
data class RegionalClearanceProfile(
    var code: String,
    var title: String,
    var weight: Double,
    var ceiling: Double,
    var graceDays: Int,
    var active: Boolean,
    var maximum: Double,
    var country: String,
    var status: RegionalClearanceProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalClearanceProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalClearanceProfileStatus): Boolean {
        when (status) {
            RegionalClearanceProfileStatus.DRAFT -> return next == RegionalClearanceProfileStatus.CLOSED
            RegionalClearanceProfileStatus.PENDING -> return next == RegionalClearanceProfileStatus.SUSPENDED
            RegionalClearanceProfileStatus.ACTIVE -> {
                when (next) {
                    RegionalClearanceProfileStatus.SUSPENDED, RegionalClearanceProfileStatus.CLOSED -> return true
                }
                return false
            }
            RegionalClearanceProfileStatus.SUSPENDED -> return next == RegionalClearanceProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalClearanceProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional clearance profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((weight + ceiling) * 100) / 100

    fun snapshot(): RegionalClearanceProfile {
        return RegionalClearanceProfile(
            code = code,
            title = title,
            weight = weight,
            ceiling = ceiling,
            graceDays = graceDays,
            active = active,
            maximum = maximum,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional clearance profile in status draft. */
fun newRegionalClearanceProfile(code: String, title: String): RegionalClearanceProfile {
    return RegionalClearanceProfile(
        code = code,
        title = title,
        weight = 0.0,
        ceiling = 0.0,
        graceDays = 0,
        active = false,
        maximum = 0.0,
        country = "",
        status = RegionalClearanceProfileStatus.DRAFT,
    )
}
