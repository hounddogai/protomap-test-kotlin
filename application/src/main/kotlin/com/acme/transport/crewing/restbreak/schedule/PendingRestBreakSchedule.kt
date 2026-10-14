package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending rest break schedule records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a pending rest break schedule is in its life cycle. */
enum class PendingRestBreakScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending rest break schedule. Its code is unique within a tenant. */
data class PendingRestBreakSchedule(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var endsAt: Int,
    var batchSize: Int,
    var fuelLiters: Double,
    var channel: String,
    var currency: String,
    var description: String,
    var region: String,
    var active: Boolean,
    var status: PendingRestBreakScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRestBreakScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRestBreakScheduleStatus): Boolean {
        when (status) {
            PendingRestBreakScheduleStatus.PLANNED -> {
                when (next) {
                    PendingRestBreakScheduleStatus.SCHEDULED, PendingRestBreakScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRestBreakScheduleStatus.SCHEDULED -> {
                when (next) {
                    PendingRestBreakScheduleStatus.IN_PROGRESS, PendingRestBreakScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            PendingRestBreakScheduleStatus.IN_PROGRESS -> return next == PendingRestBreakScheduleStatus.CANCELLED
            PendingRestBreakScheduleStatus.COMPLETED -> {
                when (next) {
                    PendingRestBreakScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRestBreakScheduleStatus) {
        if (!canMoveTo(next)) {
            error("pending rest break schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): PendingRestBreakSchedule {
        return PendingRestBreakSchedule(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            channel = channel,
            currency = currency,
            description = description,
            region = region,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending rest break schedule in status planned. */
fun newPendingRestBreakSchedule(code: String, name: String): PendingRestBreakSchedule {
    return PendingRestBreakSchedule(
        code = code,
        name = name,
        etaMinutes = 0,
        endsAt = 0,
        batchSize = 0,
        fuelLiters = 0.0,
        channel = "",
        currency = "",
        description = "",
        region = "",
        active = false,
        status = PendingRestBreakScheduleStatus.PLANNED,
    )
}
