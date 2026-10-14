package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default duty period milestone is in its life cycle. */
enum class DefaultDutyPeriodMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default duty period milestone and its current status. */
data class DefaultDutyPeriodMilestone(
    var code: String,
    var name: String,
    var rate: Double,
    var visible: Boolean,
    var endsAt: Int,
    var shiftCode: String,
    var status: DefaultDutyPeriodMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDutyPeriodMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDutyPeriodMilestoneStatus): Boolean {
        when (status) {
            DefaultDutyPeriodMilestoneStatus.IDLE -> {
                when (next) {
                    DefaultDutyPeriodMilestoneStatus.READY, DefaultDutyPeriodMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            DefaultDutyPeriodMilestoneStatus.WARMING -> return next == DefaultDutyPeriodMilestoneStatus.DRAINING
            DefaultDutyPeriodMilestoneStatus.READY -> {
                when (next) {
                    DefaultDutyPeriodMilestoneStatus.DRAINING, DefaultDutyPeriodMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            DefaultDutyPeriodMilestoneStatus.DRAINING -> {
                when (next) {
                    DefaultDutyPeriodMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDutyPeriodMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("default duty period milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default duty period milestone"} $code"

    fun netTotal(): Double = round(rate * endsAt.toDouble() * 100) / 100

    fun snapshot(): DefaultDutyPeriodMilestone {
        return DefaultDutyPeriodMilestone(
            code = code,
            name = name,
            rate = rate,
            visible = visible,
            endsAt = endsAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default duty period milestone in status idle. */
fun newDefaultDutyPeriodMilestone(code: String, name: String): DefaultDutyPeriodMilestone {
    return DefaultDutyPeriodMilestone(
        code = code,
        name = name,
        rate = 0.0,
        visible = false,
        endsAt = 0,
        shiftCode = "",
        status = DefaultDutyPeriodMilestoneStatus.IDLE,
    )
}
