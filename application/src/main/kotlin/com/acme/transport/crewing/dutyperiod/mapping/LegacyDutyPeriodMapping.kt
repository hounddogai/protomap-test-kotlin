package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.text.normalize

/** Where a legacy duty period mapping is in its life cycle. */
enum class LegacyDutyPeriodMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy duty period mapping as the duty period team keeps it. */
data class LegacyDutyPeriodMapping(
    var code: String,
    var title: String,
    var priority: Int,
    var notes: String,
    var summary: String,
    var shiftCode: String,
    var lane: String,
    var channel: String,
    var active: Boolean,
    var timezone: String,
    var checksum: String,
    var status: LegacyDutyPeriodMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDutyPeriodMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDutyPeriodMappingStatus): Boolean {
        when (status) {
            LegacyDutyPeriodMappingStatus.DRAFT -> return next == LegacyDutyPeriodMappingStatus.CLOSED
            LegacyDutyPeriodMappingStatus.PENDING -> {
                when (next) {
                    LegacyDutyPeriodMappingStatus.ACTIVE, LegacyDutyPeriodMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyDutyPeriodMappingStatus.ACTIVE -> {
                when (next) {
                    LegacyDutyPeriodMappingStatus.SUSPENDED, LegacyDutyPeriodMappingStatus.CLOSED -> return true
                }
                return false
            }
            LegacyDutyPeriodMappingStatus.SUSPENDED -> {
                when (next) {
                    LegacyDutyPeriodMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDutyPeriodMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy duty period mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): LegacyDutyPeriodMapping {
        return LegacyDutyPeriodMapping(
            code = code,
            title = title,
            priority = priority,
            notes = notes,
            summary = summary,
            shiftCode = shiftCode,
            lane = lane,
            channel = channel,
            active = active,
            timezone = timezone,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy duty period mapping in status draft. */
fun newLegacyDutyPeriodMapping(code: String, title: String): LegacyDutyPeriodMapping {
    return LegacyDutyPeriodMapping(
        code = code,
        title = title,
        priority = 0,
        notes = "",
        summary = "",
        shiftCode = "",
        lane = "",
        channel = "",
        active = false,
        timezone = "",
        checksum = "",
        status = LegacyDutyPeriodMappingStatus.DRAFT,
    )
}
