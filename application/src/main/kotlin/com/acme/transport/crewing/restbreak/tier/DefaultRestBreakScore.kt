package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default rest break score is in its life cycle. */
enum class DefaultRestBreakScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default rest break score. Its code is unique within a tenant. */
data class DefaultRestBreakScore(
    var code: String,
    var label: String,
    var startsAt: Int,
    var priority: Int,
    var etaMinutes: Int,
    var approved: Boolean,
    var title: String,
    var loadKg: Double,
    var shiftCode: String,
    var timezone: String,
    var segment: String,
    var status: DefaultRestBreakScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRestBreakScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRestBreakScoreStatus): Boolean {
        when (status) {
            DefaultRestBreakScoreStatus.IDLE -> {
                when (next) {
                    DefaultRestBreakScoreStatus.READY, DefaultRestBreakScoreStatus.DRAINING -> return true
                }
                return false
            }
            DefaultRestBreakScoreStatus.WARMING -> return next == DefaultRestBreakScoreStatus.READY || next == DefaultRestBreakScoreStatus.DRAINING
            DefaultRestBreakScoreStatus.READY -> return next == DefaultRestBreakScoreStatus.DRAINING || next == DefaultRestBreakScoreStatus.STOPPED
            DefaultRestBreakScoreStatus.DRAINING -> return next == DefaultRestBreakScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultRestBreakScoreStatus) {
        if (!canMoveTo(next)) {
            error("default rest break score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round((priority.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultRestBreakScore {
        return DefaultRestBreakScore(
            code = code,
            label = label,
            startsAt = startsAt,
            priority = priority,
            etaMinutes = etaMinutes,
            approved = approved,
            title = title,
            loadKg = loadKg,
            shiftCode = shiftCode,
            timezone = timezone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default rest break score in status idle. */
fun newDefaultRestBreakScore(code: String, label: String): DefaultRestBreakScore {
    return DefaultRestBreakScore(
        code = code,
        label = label,
        startsAt = 0,
        priority = 0,
        etaMinutes = 0,
        approved = false,
        title = "",
        loadKg = 0.0,
        shiftCode = "",
        timezone = "",
        segment = "",
        status = DefaultRestBreakScoreStatus.IDLE,
    )
}
