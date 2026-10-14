package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.text.normalize

/** Where a redelivery estimate is in its life cycle. */
enum class RedeliveryEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a redelivery estimate and its current status. */
data class RedeliveryEstimate(
    var code: String,
    var name: String,
    var visible: Boolean,
    var sequence: Int,
    var reviewer: String,
    var status: RedeliveryEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryEstimateStatus): Boolean {
        when (status) {
            RedeliveryEstimateStatus.PLANNED -> {
                when (next) {
                    RedeliveryEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryEstimateStatus.SCHEDULED -> {
                when (next) {
                    RedeliveryEstimateStatus.COMPLETED, RedeliveryEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    RedeliveryEstimateStatus.COMPLETED, RedeliveryEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryEstimateStatus.COMPLETED -> return next == RedeliveryEstimateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RedeliveryEstimateStatus) {
        if (!canMoveTo(next)) {
            error("redelivery estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): RedeliveryEstimate {
        return RedeliveryEstimate(
            code = code,
            name = name,
            visible = visible,
            sequence = sequence,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery estimate in status planned. */
fun newRedeliveryEstimate(code: String, name: String): RedeliveryEstimate {
    return RedeliveryEstimate(
        code = code,
        name = name,
        visible = false,
        sequence = 0,
        reviewer = "",
        status = RedeliveryEstimateStatus.PLANNED,
    )
}
