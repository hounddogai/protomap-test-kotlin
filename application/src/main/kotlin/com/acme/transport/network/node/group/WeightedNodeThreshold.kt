package com.acme.transport.network.node.group

import com.acme.transport.kit.text.normalize

/** Where a weighted node threshold is in its life cycle. */
enum class WeightedNodeThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a weighted node threshold and its current status. */
data class WeightedNodeThreshold(
    var code: String,
    var label: String,
    var unit: String,
    var startsAt: Int,
    var archived: Boolean,
    var status: WeightedNodeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedNodeThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedNodeThresholdStatus): Boolean {
        when (status) {
            WeightedNodeThresholdStatus.PROPOSED -> return next == WeightedNodeThresholdStatus.REJECTED
            WeightedNodeThresholdStatus.APPROVED -> return next == WeightedNodeThresholdStatus.REJECTED || next == WeightedNodeThresholdStatus.WITHDRAWN
            WeightedNodeThresholdStatus.REJECTED -> {
                when (next) {
                    WeightedNodeThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedNodeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted node threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted node threshold"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): WeightedNodeThreshold {
        return WeightedNodeThreshold(
            code = code,
            label = label,
            unit = unit,
            startsAt = startsAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted node threshold in status proposed. */
fun newWeightedNodeThreshold(code: String, label: String): WeightedNodeThreshold {
    return WeightedNodeThreshold(
        code = code,
        label = label,
        unit = "",
        startsAt = 0,
        archived = false,
        status = WeightedNodeThresholdStatus.PROPOSED,
    )
}
