package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard mapping is in its life cycle. */
enum class ScorecardMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scorecard mapping, with what the planners need to act on it. */
data class ScorecardMapping(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var retries: Int,
    var startsAt: Int,
    var visible: Boolean,
    var phone: String,
    var status: ScorecardMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardMappingStatus): Boolean {
        when (status) {
            ScorecardMappingStatus.PROPOSED -> {
                when (next) {
                    ScorecardMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardMappingStatus.APPROVED -> return next == ScorecardMappingStatus.REJECTED || next == ScorecardMappingStatus.WITHDRAWN
            ScorecardMappingStatus.REJECTED -> {
                when (next) {
                    ScorecardMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardMappingStatus) {
        if (!canMoveTo(next)) {
            error("scorecard mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round(startsAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): ScorecardMapping {
        return ScorecardMapping(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            retries = retries,
            startsAt = startsAt,
            visible = visible,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard mapping in status proposed. */
fun newScorecardMapping(code: String, label: String): ScorecardMapping {
    return ScorecardMapping(
        code = code,
        label = label,
        durationMinutes = 0,
        retries = 0,
        startsAt = 0,
        visible = false,
        phone = "",
        status = ScorecardMappingStatus.PROPOSED,
    )
}
