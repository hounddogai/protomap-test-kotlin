package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge target is in its life cycle. */
enum class EvChargeTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One ev charge target, with what the planners need to act on it. */
data class EvChargeTarget(
    var code: String,
    var label: String,
    var region: String,
    var timezone: String,
    var driverId: String,
    var country: String,
    var locale: String,
    var priority: Int,
    var reviewer: String,
    var floorValue: Double,
    var minimum: Double,
    var status: EvChargeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeTargetStatus): Boolean {
        when (status) {
            EvChargeTargetStatus.IDLE -> {
                when (next) {
                    EvChargeTargetStatus.READY, EvChargeTargetStatus.DRAINING -> return true
                }
                return false
            }
            EvChargeTargetStatus.WARMING -> {
                when (next) {
                    EvChargeTargetStatus.READY -> return true
                }
                return false
            }
            EvChargeTargetStatus.READY -> return next == EvChargeTargetStatus.DRAINING || next == EvChargeTargetStatus.STOPPED
            EvChargeTargetStatus.DRAINING -> {
                when (next) {
                    EvChargeTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeTargetStatus) {
        if (!canMoveTo(next)) {
            error("ev charge target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round((priority.toDouble() + minimum) * 100) / 100

    fun snapshot(): EvChargeTarget {
        return EvChargeTarget(
            code = code,
            label = label,
            region = region,
            timezone = timezone,
            driverId = driverId,
            country = country,
            locale = locale,
            priority = priority,
            reviewer = reviewer,
            floorValue = floorValue,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a ev charge target in status idle. */
fun newEvChargeTarget(code: String, label: String): EvChargeTarget {
    return EvChargeTarget(
        code = code,
        label = label,
        region = "",
        timezone = "",
        driverId = "",
        country = "",
        locale = "",
        priority = 0,
        reviewer = "",
        floorValue = 0.0,
        minimum = 0.0,
        status = EvChargeTargetStatus.IDLE,
    )
}
