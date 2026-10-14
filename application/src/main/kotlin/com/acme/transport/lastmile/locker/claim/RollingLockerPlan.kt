package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling locker plan is in its life cycle. */
enum class RollingLockerPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling locker plan. Its code is unique within a tenant. */
data class RollingLockerPlan(
    var code: String,
    var title: String,
    var segment: String,
    var shiftCode: String,
    var maximum: Double,
    var description: String,
    var durationMinutes: Int,
    var trailerId: String,
    var endsAt: Int,
    var batchSize: Int,
    var status: RollingLockerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLockerPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLockerPlanStatus): Boolean {
        when (status) {
            RollingLockerPlanStatus.NEW -> {
                when (next) {
                    RollingLockerPlanStatus.QUEUED, RollingLockerPlanStatus.FAILED -> return true
                }
                return false
            }
            RollingLockerPlanStatus.QUEUED -> return next == RollingLockerPlanStatus.DONE || next == RollingLockerPlanStatus.FAILED
            RollingLockerPlanStatus.RUNNING -> {
                when (next) {
                    RollingLockerPlanStatus.DONE -> return true
                }
                return false
            }
            RollingLockerPlanStatus.DONE -> return next == RollingLockerPlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingLockerPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling locker plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling locker plan"} $code"

    fun effectiveTotal(): Double = round((batchSize.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): RollingLockerPlan {
        return RollingLockerPlan(
            code = code,
            title = title,
            segment = segment,
            shiftCode = shiftCode,
            maximum = maximum,
            description = description,
            durationMinutes = durationMinutes,
            trailerId = trailerId,
            endsAt = endsAt,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling locker plan in status new. */
fun newRollingLockerPlan(code: String, title: String): RollingLockerPlan {
    return RollingLockerPlan(
        code = code,
        title = title,
        segment = "",
        shiftCode = "",
        maximum = 0.0,
        description = "",
        durationMinutes = 0,
        trailerId = "",
        endsAt = 0,
        batchSize = 0,
        status = RollingLockerPlanStatus.NEW,
    )
}
