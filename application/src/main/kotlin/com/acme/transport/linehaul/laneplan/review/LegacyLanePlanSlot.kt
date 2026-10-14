package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lane plan slot is in its life cycle. */
enum class LegacyLanePlanSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy lane plan slot as the lane plan team keeps it. */
data class LegacyLanePlanSlot(
    var code: String,
    var name: String,
    var vehicleId: String,
    var startsAt: Int,
    var minimum: Double,
    var status: LegacyLanePlanSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLanePlanSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLanePlanSlotStatus): Boolean {
        when (status) {
            LegacyLanePlanSlotStatus.NEW -> return next == LegacyLanePlanSlotStatus.DONE || next == LegacyLanePlanSlotStatus.FAILED
            LegacyLanePlanSlotStatus.QUEUED -> return next == LegacyLanePlanSlotStatus.RUNNING || next == LegacyLanePlanSlotStatus.DONE
            LegacyLanePlanSlotStatus.RUNNING -> {
                when (next) {
                    LegacyLanePlanSlotStatus.DONE, LegacyLanePlanSlotStatus.FAILED -> return true
                }
                return false
            }
            LegacyLanePlanSlotStatus.DONE -> return next == LegacyLanePlanSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyLanePlanSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy lane plan slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((minimum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyLanePlanSlot {
        return LegacyLanePlanSlot(
            code = code,
            name = name,
            vehicleId = vehicleId,
            startsAt = startsAt,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy lane plan slot in status new. */
fun newLegacyLanePlanSlot(code: String, name: String): LegacyLanePlanSlot {
    return LegacyLanePlanSlot(
        code = code,
        name = name,
        vehicleId = "",
        startsAt = 0,
        minimum = 0.0,
        status = LegacyLanePlanSlotStatus.NEW,
    )
}
