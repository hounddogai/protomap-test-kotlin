package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched relay plan is in its life cycle. */
enum class BatchedRelayPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched relay plan and its current status. */
data class BatchedRelayPlan(
    var code: String,
    var label: String,
    var reviewer: String,
    var shiftCode: String,
    var carrier: String,
    var visible: Boolean,
    var startsAt: Int,
    var trailerId: String,
    var limit: Int,
    var durationMinutes: Int,
    var timezone: String,
    var status: BatchedRelayPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRelayPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRelayPlanStatus): Boolean {
        when (status) {
            BatchedRelayPlanStatus.DRAFT -> {
                when (next) {
                    BatchedRelayPlanStatus.ACTIVE, BatchedRelayPlanStatus.CLOSED -> return true
                }
                return false
            }
            BatchedRelayPlanStatus.PENDING -> return next == BatchedRelayPlanStatus.ACTIVE
            BatchedRelayPlanStatus.ACTIVE -> {
                when (next) {
                    BatchedRelayPlanStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedRelayPlanStatus.SUSPENDED -> return next == BatchedRelayPlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedRelayPlanStatus) {
        if (!canMoveTo(next)) {
            error("batched relay plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round(durationMinutes.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): BatchedRelayPlan {
        return BatchedRelayPlan(
            code = code,
            label = label,
            reviewer = reviewer,
            shiftCode = shiftCode,
            carrier = carrier,
            visible = visible,
            startsAt = startsAt,
            trailerId = trailerId,
            limit = limit,
            durationMinutes = durationMinutes,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched relay plan in status draft. */
fun newBatchedRelayPlan(code: String, label: String): BatchedRelayPlan {
    return BatchedRelayPlan(
        code = code,
        label = label,
        reviewer = "",
        shiftCode = "",
        carrier = "",
        visible = false,
        startsAt = 0,
        trailerId = "",
        limit = 0,
        durationMinutes = 0,
        timezone = "",
        status = BatchedRelayPlanStatus.DRAFT,
    )
}
