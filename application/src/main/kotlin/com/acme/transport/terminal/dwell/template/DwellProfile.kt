package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell profile is in its life cycle. */
enum class DwellProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One dwell profile, with what the planners need to act on it. */
data class DwellProfile(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var score: Double,
    var durationMinutes: Int,
    var ceiling: Double,
    var limit: Int,
    var minimum: Double,
    var status: DwellProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellProfileStatus): Boolean {
        when (status) {
            DwellProfileStatus.REQUESTED -> return next == DwellProfileStatus.GRANTED || next == DwellProfileStatus.REVOKED
            DwellProfileStatus.GRANTED -> return next == DwellProfileStatus.REVOKED
            DwellProfileStatus.REVOKED -> {
                when (next) {
                    DwellProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellProfileStatus) {
        if (!canMoveTo(next)) {
            error("dwell profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dwell profile"} $code"

    fun netValue(): Double = round((durationMinutes.toDouble() + score) * 100) / 100

    fun snapshot(): DwellProfile {
        return DwellProfile(
            code = code,
            title = title,
            pinned = pinned,
            score = score,
            durationMinutes = durationMinutes,
            ceiling = ceiling,
            limit = limit,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell profile in status requested. */
fun newDwellProfile(code: String, title: String): DwellProfile {
    return DwellProfile(
        code = code,
        title = title,
        pinned = false,
        score = 0.0,
        durationMinutes = 0,
        ceiling = 0.0,
        limit = 0,
        minimum = 0.0,
        status = DwellProfileStatus.REQUESTED,
    )
}
