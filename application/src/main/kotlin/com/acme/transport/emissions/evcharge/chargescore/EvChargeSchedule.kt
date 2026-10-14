package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge schedule is in its life cycle. */
enum class EvChargeScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a ev charge schedule and its current status. */
data class EvChargeSchedule(
    var code: String,
    var label: String,
    var ceiling: Double,
    var distanceKm: Double,
    var expiresAt: Int,
    var batchSize: Int,
    var status: EvChargeScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeScheduleStatus): Boolean {
        when (status) {
            EvChargeScheduleStatus.PROPOSED -> {
                when (next) {
                    EvChargeScheduleStatus.APPROVED, EvChargeScheduleStatus.REJECTED -> return true
                }
                return false
            }
            EvChargeScheduleStatus.APPROVED -> {
                when (next) {
                    EvChargeScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            EvChargeScheduleStatus.REJECTED -> {
                when (next) {
                    EvChargeScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeScheduleStatus) {
        if (!canMoveTo(next)) {
            error("ev charge schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round(expiresAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): EvChargeSchedule {
        return EvChargeSchedule(
            code = code,
            label = label,
            ceiling = ceiling,
            distanceKm = distanceKm,
            expiresAt = expiresAt,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a ev charge schedule in status proposed. */
fun newEvChargeSchedule(code: String, label: String): EvChargeSchedule {
    return EvChargeSchedule(
        code = code,
        label = label,
        ceiling = 0.0,
        distanceKm = 0.0,
        expiresAt = 0,
        batchSize = 0,
        status = EvChargeScheduleStatus.PROPOSED,
    )
}
