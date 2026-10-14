package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dwell schedule is in its life cycle. */
enum class CappedDwellScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped dwell schedule and its current status. */
data class CappedDwellSchedule(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var startsAt: Int,
    var limit: Int,
    var status: CappedDwellScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDwellScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDwellScheduleStatus): Boolean {
        when (status) {
            CappedDwellScheduleStatus.PROPOSED -> {
                when (next) {
                    CappedDwellScheduleStatus.APPROVED, CappedDwellScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedDwellScheduleStatus.APPROVED -> return next == CappedDwellScheduleStatus.REJECTED
            CappedDwellScheduleStatus.REJECTED -> return next == CappedDwellScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedDwellScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped dwell schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped dwell schedule"} $code"

    fun projectedTotal(): Double = round((etaMinutes.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): CappedDwellSchedule {
        return CappedDwellSchedule(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped dwell schedule in status proposed. */
fun newCappedDwellSchedule(code: String, label: String): CappedDwellSchedule {
    return CappedDwellSchedule(
        code = code,
        label = label,
        etaMinutes = 0,
        startsAt = 0,
        limit = 0,
        status = CappedDwellScheduleStatus.PROPOSED,
    )
}
