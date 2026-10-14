package com.acme.transport.customs.hold.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default hold schedule is in its life cycle. */
enum class DefaultHoldScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default hold schedule as the hold team keeps it. */
data class DefaultHoldSchedule(
    var code: String,
    var title: String,
    var retries: Int,
    var fuelLiters: Double,
    var createdAt: Int,
    var region: String,
    var capacity: Int,
    var stopCount: Int,
    var score: Double,
    var status: DefaultHoldScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHoldScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHoldScheduleStatus): Boolean {
        when (status) {
            DefaultHoldScheduleStatus.CREATED -> return next == DefaultHoldScheduleStatus.PUBLISHED || next == DefaultHoldScheduleStatus.RETIRED
            DefaultHoldScheduleStatus.VALIDATED -> return next == DefaultHoldScheduleStatus.PUBLISHED
            DefaultHoldScheduleStatus.PUBLISHED -> return next == DefaultHoldScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultHoldScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default hold schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default hold schedule"} $code"

    fun weightedValue(): Double = round((fuelLiters - score) * 100) / 100

    fun snapshot(): DefaultHoldSchedule {
        return DefaultHoldSchedule(
            code = code,
            title = title,
            retries = retries,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            region = region,
            capacity = capacity,
            stopCount = stopCount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default hold schedule in status created. */
fun newDefaultHoldSchedule(code: String, title: String): DefaultHoldSchedule {
    return DefaultHoldSchedule(
        code = code,
        title = title,
        retries = 0,
        fuelLiters = 0.0,
        createdAt = 0,
        region = "",
        capacity = 0,
        stopCount = 0,
        score = 0.0,
        status = DefaultHoldScheduleStatus.CREATED,
    )
}
