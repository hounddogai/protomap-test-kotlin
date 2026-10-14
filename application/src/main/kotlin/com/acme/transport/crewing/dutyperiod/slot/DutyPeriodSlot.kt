package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period slot records of crewing's duty period work, and the rules, stores, and calculations around
// them.

/** Where a duty period slot is in its life cycle. */
enum class DutyPeriodSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One duty period slot, with what the planners need to act on it. */
data class DutyPeriodSlot(
    var code: String,
    var name: String,
    var region: String,
    var country: String,
    var title: String,
    var windowDays: Int,
    var loadKg: Double,
    var status: DutyPeriodSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodSlotStatus): Boolean {
        when (status) {
            DutyPeriodSlotStatus.PLANNED -> return next == DutyPeriodSlotStatus.SCHEDULED
            DutyPeriodSlotStatus.SCHEDULED -> {
                when (next) {
                    DutyPeriodSlotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DutyPeriodSlotStatus.IN_PROGRESS -> return next == DutyPeriodSlotStatus.COMPLETED || next == DutyPeriodSlotStatus.CANCELLED
            DutyPeriodSlotStatus.COMPLETED -> {
                when (next) {
                    DutyPeriodSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodSlotStatus) {
        if (!canMoveTo(next)) {
            error("duty period slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netLoad(): Double = round(loadKg * windowDays.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodSlot {
        return DutyPeriodSlot(
            code = code,
            name = name,
            region = region,
            country = country,
            title = title,
            windowDays = windowDays,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a duty period slot in status planned. */
fun newDutyPeriodSlot(code: String, name: String): DutyPeriodSlot {
    return DutyPeriodSlot(
        code = code,
        name = name,
        region = "",
        country = "",
        title = "",
        windowDays = 0,
        loadKg = 0.0,
        status = DutyPeriodSlotStatus.PLANNED,
    )
}
