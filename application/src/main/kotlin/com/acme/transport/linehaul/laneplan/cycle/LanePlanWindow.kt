package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan window is in its life cycle. */
enum class LanePlanWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lane plan window and its current status. */
data class LanePlanWindow(
    var code: String,
    var name: String,
    var source: String,
    var lane: String,
    var graceDays: Int,
    var active: Boolean,
    var fuelLiters: Double,
    var endsAt: Int,
    var odometer: Int,
    var priority: Int,
    var status: LanePlanWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanWindowStatus): Boolean {
        when (status) {
            LanePlanWindowStatus.REQUESTED -> return next == LanePlanWindowStatus.EXPIRED
            LanePlanWindowStatus.GRANTED -> {
                when (next) {
                    LanePlanWindowStatus.REVOKED, LanePlanWindowStatus.EXPIRED -> return true
                }
                return false
            }
            LanePlanWindowStatus.REVOKED -> {
                when (next) {
                    LanePlanWindowStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanWindowStatus) {
        if (!canMoveTo(next)) {
            error("lane plan window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lane plan window"} $code"

    fun adjustedLoad(): Double = round((fuelLiters + priority.toDouble()) * 100) / 100

    fun snapshot(): LanePlanWindow {
        return LanePlanWindow(
            code = code,
            name = name,
            source = source,
            lane = lane,
            graceDays = graceDays,
            active = active,
            fuelLiters = fuelLiters,
            endsAt = endsAt,
            odometer = odometer,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lane plan window in status requested. */
fun newLanePlanWindow(code: String, name: String): LanePlanWindow {
    return LanePlanWindow(
        code = code,
        name = name,
        source = "",
        lane = "",
        graceDays = 0,
        active = false,
        fuelLiters = 0.0,
        endsAt = 0,
        odometer = 0,
        priority = 0,
        status = LanePlanWindowStatus.REQUESTED,
    )
}
