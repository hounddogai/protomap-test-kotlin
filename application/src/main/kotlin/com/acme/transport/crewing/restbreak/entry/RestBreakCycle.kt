package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break cycle is in its life cycle. */
enum class RestBreakCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rest break cycle and its current status. */
data class RestBreakCycle(
    var code: String,
    var title: String,
    var carrier: String,
    var limit: Int,
    var lane: String,
    var fuelLiters: Double,
    var createdAt: Int,
    var status: RestBreakCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakCycleStatus): Boolean {
        when (status) {
            RestBreakCycleStatus.OPEN -> {
                when (next) {
                    RestBreakCycleStatus.IN_REVIEW, RestBreakCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            RestBreakCycleStatus.IN_REVIEW -> return next == RestBreakCycleStatus.RESOLVED
            RestBreakCycleStatus.RESOLVED -> {
                when (next) {
                    RestBreakCycleStatus.REOPENED, RestBreakCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            RestBreakCycleStatus.REOPENED -> return next == RestBreakCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RestBreakCycleStatus) {
        if (!canMoveTo(next)) {
            error("rest break cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((fuelLiters - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakCycle {
        return RestBreakCycle(
            code = code,
            title = title,
            carrier = carrier,
            limit = limit,
            lane = lane,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rest break cycle in status open. */
fun newRestBreakCycle(code: String, title: String): RestBreakCycle {
    return RestBreakCycle(
        code = code,
        title = title,
        carrier = "",
        limit = 0,
        lane = "",
        fuelLiters = 0.0,
        createdAt = 0,
        status = RestBreakCycleStatus.OPEN,
    )
}
