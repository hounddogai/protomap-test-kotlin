package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling violation batch is in its life cycle. */
enum class RollingViolationBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling violation batch and its current status. */
data class RollingViolationBatch(
    var code: String,
    var label: String,
    var routeCode: String,
    var endsAt: Int,
    var locale: String,
    var startsAt: Int,
    var score: Double,
    var discount: Double,
    var vehicleId: String,
    var status: RollingViolationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationBatchStatus): Boolean {
        when (status) {
            RollingViolationBatchStatus.PROPOSED -> {
                when (next) {
                    RollingViolationBatchStatus.APPROVED -> return true
                }
                return false
            }
            RollingViolationBatchStatus.APPROVED -> return next == RollingViolationBatchStatus.REJECTED || next == RollingViolationBatchStatus.WITHDRAWN
            RollingViolationBatchStatus.REJECTED -> {
                when (next) {
                    RollingViolationBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingViolationBatchStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling violation batch"} $code"

    fun weightedTotal(): Double = round(score * discount * 100) / 100

    fun snapshot(): RollingViolationBatch {
        return RollingViolationBatch(
            code = code,
            label = label,
            routeCode = routeCode,
            endsAt = endsAt,
            locale = locale,
            startsAt = startsAt,
            score = score,
            discount = discount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling violation batch in status proposed. */
fun newRollingViolationBatch(code: String, label: String): RollingViolationBatch {
    return RollingViolationBatch(
        code = code,
        label = label,
        routeCode = "",
        endsAt = 0,
        locale = "",
        startsAt = 0,
        score = 0.0,
        discount = 0.0,
        vehicleId = "",
        status = RollingViolationBatchStatus.PROPOSED,
    )
}
