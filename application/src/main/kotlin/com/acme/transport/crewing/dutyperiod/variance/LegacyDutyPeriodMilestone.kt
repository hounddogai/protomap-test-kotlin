package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy duty period milestone is in its life cycle. */
enum class LegacyDutyPeriodMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy duty period milestone and its current status. */
data class LegacyDutyPeriodMilestone(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var rate: Double,
    var checksum: String,
    var timezone: String,
    var quantity: Int,
    var minimum: Double,
    var description: String,
    var amount: Double,
    var status: LegacyDutyPeriodMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDutyPeriodMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDutyPeriodMilestoneStatus): Boolean {
        when (status) {
            LegacyDutyPeriodMilestoneStatus.DRAFT -> return next == LegacyDutyPeriodMilestoneStatus.PENDING || next == LegacyDutyPeriodMilestoneStatus.CLOSED
            LegacyDutyPeriodMilestoneStatus.PENDING -> {
                when (next) {
                    LegacyDutyPeriodMilestoneStatus.ACTIVE -> return true
                }
                return false
            }
            LegacyDutyPeriodMilestoneStatus.ACTIVE -> {
                when (next) {
                    LegacyDutyPeriodMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyDutyPeriodMilestoneStatus.SUSPENDED -> {
                when (next) {
                    LegacyDutyPeriodMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDutyPeriodMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("legacy duty period milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((minimum - quantity.toDouble()) * 100) / 100

    fun snapshot(): LegacyDutyPeriodMilestone {
        return LegacyDutyPeriodMilestone(
            code = code,
            label = label,
            tags = tags,
            rate = rate,
            checksum = checksum,
            timezone = timezone,
            quantity = quantity,
            minimum = minimum,
            description = description,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy duty period milestone in status draft. */
fun newLegacyDutyPeriodMilestone(code: String, label: String): LegacyDutyPeriodMilestone {
    return LegacyDutyPeriodMilestone(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        rate = 0.0,
        checksum = "",
        timezone = "",
        quantity = 0,
        minimum = 0.0,
        description = "",
        amount = 0.0,
        status = LegacyDutyPeriodMilestoneStatus.DRAFT,
    )
}
