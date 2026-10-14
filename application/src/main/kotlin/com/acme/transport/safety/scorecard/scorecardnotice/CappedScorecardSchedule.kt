package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped scorecard schedule is in its life cycle. */
enum class CappedScorecardScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped scorecard schedule as the scorecard team keeps it. */
data class CappedScorecardSchedule(
    var code: String,
    var name: String,
    var routeCode: String,
    var ratio: Double,
    var channel: String,
    var maximum: Double,
    var shiftCode: String,
    var quantity: Int,
    var label: String,
    var attributes: MutableMap<String, String>,
    var category: String,
    var status: CappedScorecardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardScheduleStatus): Boolean {
        when (status) {
            CappedScorecardScheduleStatus.DRAFT -> return next == CappedScorecardScheduleStatus.ACTIVE || next == CappedScorecardScheduleStatus.CLOSED
            CappedScorecardScheduleStatus.PENDING -> {
                when (next) {
                    CappedScorecardScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedScorecardScheduleStatus.ACTIVE -> return next == CappedScorecardScheduleStatus.SUSPENDED
            CappedScorecardScheduleStatus.SUSPENDED -> return next == CappedScorecardScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedScorecardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((ratio + maximum) * 100) / 100

    fun snapshot(): CappedScorecardSchedule {
        return CappedScorecardSchedule(
            code = code,
            name = name,
            routeCode = routeCode,
            ratio = ratio,
            channel = channel,
            maximum = maximum,
            shiftCode = shiftCode,
            quantity = quantity,
            label = label,
            attributes = attributes,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped scorecard schedule in status draft. */
fun newCappedScorecardSchedule(code: String, name: String): CappedScorecardSchedule {
    return CappedScorecardSchedule(
        code = code,
        name = name,
        routeCode = "",
        ratio = 0.0,
        channel = "",
        maximum = 0.0,
        shiftCode = "",
        quantity = 0,
        label = "",
        attributes = mutableMapOf<String, String>(),
        category = "",
        status = CappedScorecardScheduleStatus.DRAFT,
    )
}
