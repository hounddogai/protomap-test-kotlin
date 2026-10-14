package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional violation profile is in its life cycle. */
enum class RegionalViolationProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional violation profile. Its code is unique within a tenant. */
data class RegionalViolationProfile(
    var code: String,
    var name: String,
    var timezone: String,
    var ceiling: Double,
    var weight: Double,
    var status: RegionalViolationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalViolationProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalViolationProfileStatus): Boolean {
        when (status) {
            RegionalViolationProfileStatus.IDLE -> return next == RegionalViolationProfileStatus.WARMING || next == RegionalViolationProfileStatus.READY
            RegionalViolationProfileStatus.WARMING -> return next == RegionalViolationProfileStatus.STOPPED
            RegionalViolationProfileStatus.READY -> return next == RegionalViolationProfileStatus.STOPPED
            RegionalViolationProfileStatus.DRAINING -> return next == RegionalViolationProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalViolationProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional violation profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round((ceiling + weight) * 100) / 100

    fun snapshot(): RegionalViolationProfile {
        return RegionalViolationProfile(
            code = code,
            name = name,
            timezone = timezone,
            ceiling = ceiling,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional violation profile in status idle. */
fun newRegionalViolationProfile(code: String, name: String): RegionalViolationProfile {
    return RegionalViolationProfile(
        code = code,
        name = name,
        timezone = "",
        ceiling = 0.0,
        weight = 0.0,
        status = RegionalViolationProfileStatus.IDLE,
    )
}
