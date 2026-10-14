package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan exception records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a lane plan exception is in its life cycle. */
enum class LanePlanExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lane plan exception, with what the planners need to act on it. */
data class LanePlanException(
    var code: String,
    var title: String,
    var batchSize: Int,
    var score: Double,
    var routeCode: String,
    var driverId: String,
    var createdAt: Int,
    var sequence: Int,
    var status: LanePlanExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanExceptionStatus): Boolean {
        when (status) {
            LanePlanExceptionStatus.IDLE -> {
                when (next) {
                    LanePlanExceptionStatus.WARMING, LanePlanExceptionStatus.DRAINING -> return true
                }
                return false
            }
            LanePlanExceptionStatus.WARMING -> return next == LanePlanExceptionStatus.STOPPED
            LanePlanExceptionStatus.READY -> return next == LanePlanExceptionStatus.STOPPED
            LanePlanExceptionStatus.DRAINING -> {
                when (next) {
                    LanePlanExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanExceptionStatus) {
        if (!canMoveTo(next)) {
            error("lane plan exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round(batchSize.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): LanePlanException {
        return LanePlanException(
            code = code,
            title = title,
            batchSize = batchSize,
            score = score,
            routeCode = routeCode,
            driverId = driverId,
            createdAt = createdAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan exception in status idle. */
fun newLanePlanException(code: String, title: String): LanePlanException {
    return LanePlanException(
        code = code,
        title = title,
        batchSize = 0,
        score = 0.0,
        routeCode = "",
        driverId = "",
        createdAt = 0,
        sequence = 0,
        status = LanePlanExceptionStatus.IDLE,
    )
}
