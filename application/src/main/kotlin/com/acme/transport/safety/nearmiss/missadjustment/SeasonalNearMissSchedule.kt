package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize

/** Where a seasonal near miss schedule is in its life cycle. */
enum class SeasonalNearMissScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal near miss schedule. Its code is unique within a tenant. */
data class SeasonalNearMissSchedule(
    var code: String,
    var label: String,
    var minimum: Double,
    var checksum: String,
    var depot: String,
    var channel: String,
    var category: String,
    var shiftCode: String,
    var status: SeasonalNearMissScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNearMissScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNearMissScheduleStatus): Boolean {
        when (status) {
            SeasonalNearMissScheduleStatus.REQUESTED -> return next == SeasonalNearMissScheduleStatus.REVOKED
            SeasonalNearMissScheduleStatus.GRANTED -> {
                when (next) {
                    SeasonalNearMissScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalNearMissScheduleStatus.REVOKED -> {
                when (next) {
                    SeasonalNearMissScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalNearMissScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal near miss schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = minimum

    fun snapshot(): SeasonalNearMissSchedule {
        return SeasonalNearMissSchedule(
            code = code,
            label = label,
            minimum = minimum,
            checksum = checksum,
            depot = depot,
            channel = channel,
            category = category,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal near miss schedule in status requested. */
fun newSeasonalNearMissSchedule(code: String, label: String): SeasonalNearMissSchedule {
    return SeasonalNearMissSchedule(
        code = code,
        label = label,
        minimum = 0.0,
        checksum = "",
        depot = "",
        channel = "",
        category = "",
        shiftCode = "",
        status = SeasonalNearMissScheduleStatus.REQUESTED,
    )
}
