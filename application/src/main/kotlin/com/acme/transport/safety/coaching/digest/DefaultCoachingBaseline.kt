package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default coaching baseline is in its life cycle. */
enum class DefaultCoachingBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default coaching baseline, with what the planners need to act on it. */
data class DefaultCoachingBaseline(
    var code: String,
    var name: String,
    var pallets: Int,
    var timezone: String,
    var phone: String,
    var etaMinutes: Int,
    var minimum: Double,
    var threshold: Double,
    var windowDays: Int,
    var durationMinutes: Int,
    var visible: Boolean,
    var status: DefaultCoachingBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingBaselineStatus): Boolean {
        when (status) {
            DefaultCoachingBaselineStatus.REQUESTED -> return next == DefaultCoachingBaselineStatus.REVOKED || next == DefaultCoachingBaselineStatus.EXPIRED
            DefaultCoachingBaselineStatus.GRANTED -> return next == DefaultCoachingBaselineStatus.REVOKED || next == DefaultCoachingBaselineStatus.EXPIRED
            DefaultCoachingBaselineStatus.REVOKED -> {
                when (next) {
                    DefaultCoachingBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCoachingBaselineStatus) {
        if (!canMoveTo(next)) {
            error("default coaching baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((durationMinutes.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): DefaultCoachingBaseline {
        return DefaultCoachingBaseline(
            code = code,
            name = name,
            pallets = pallets,
            timezone = timezone,
            phone = phone,
            etaMinutes = etaMinutes,
            minimum = minimum,
            threshold = threshold,
            windowDays = windowDays,
            durationMinutes = durationMinutes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default coaching baseline in status requested. */
fun newDefaultCoachingBaseline(code: String, name: String): DefaultCoachingBaseline {
    return DefaultCoachingBaseline(
        code = code,
        name = name,
        pallets = 0,
        timezone = "",
        phone = "",
        etaMinutes = 0,
        minimum = 0.0,
        threshold = 0.0,
        windowDays = 0,
        durationMinutes = 0,
        visible = false,
        status = DefaultCoachingBaselineStatus.REQUESTED,
    )
}
