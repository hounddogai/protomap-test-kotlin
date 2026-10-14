package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event budget is in its life cycle. */
enum class CameraEventBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One camera event budget, with what the planners need to act on it. */
data class CameraEventBudget(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var batchSize: Int,
    var routeCode: String,
    var carrier: String,
    var maximum: Double,
    var graceDays: Int,
    var score: Double,
    var reviewer: String,
    var currency: String,
    var status: CameraEventBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventBudgetStatus): Boolean {
        when (status) {
            CameraEventBudgetStatus.IDLE -> return next == CameraEventBudgetStatus.WARMING || next == CameraEventBudgetStatus.READY
            CameraEventBudgetStatus.WARMING -> {
                when (next) {
                    CameraEventBudgetStatus.READY, CameraEventBudgetStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventBudgetStatus.READY -> return next == CameraEventBudgetStatus.DRAINING || next == CameraEventBudgetStatus.STOPPED
            CameraEventBudgetStatus.DRAINING -> return next == CameraEventBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CameraEventBudgetStatus) {
        if (!canMoveTo(next)) {
            error("camera event budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"camera event budget"} $code"

    fun weightedValue(): Double = round((expiresAt.toDouble() - score) * 100) / 100

    fun snapshot(): CameraEventBudget {
        return CameraEventBudget(
            code = code,
            label = label,
            expiresAt = expiresAt,
            batchSize = batchSize,
            routeCode = routeCode,
            carrier = carrier,
            maximum = maximum,
            graceDays = graceDays,
            score = score,
            reviewer = reviewer,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event budget in status idle. */
fun newCameraEventBudget(code: String, label: String): CameraEventBudget {
    return CameraEventBudget(
        code = code,
        label = label,
        expiresAt = 0,
        batchSize = 0,
        routeCode = "",
        carrier = "",
        maximum = 0.0,
        graceDays = 0,
        score = 0.0,
        reviewer = "",
        currency = "",
        status = CameraEventBudgetStatus.IDLE,
    )
}
