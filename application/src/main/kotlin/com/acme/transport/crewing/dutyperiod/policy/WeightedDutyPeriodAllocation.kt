package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted duty period allocation is in its life cycle. */
enum class WeightedDutyPeriodAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One weighted duty period allocation, with what the planners need to act on it. */
data class WeightedDutyPeriodAllocation(
    var code: String,
    var label: String,
    var ceiling: Double,
    var priority: Int,
    var carrier: String,
    var segment: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var limit: Int,
    var region: String,
    var status: WeightedDutyPeriodAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDutyPeriodAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDutyPeriodAllocationStatus): Boolean {
        when (status) {
            WeightedDutyPeriodAllocationStatus.REQUESTED -> return next == WeightedDutyPeriodAllocationStatus.GRANTED || next == WeightedDutyPeriodAllocationStatus.REVOKED
            WeightedDutyPeriodAllocationStatus.GRANTED -> {
                when (next) {
                    WeightedDutyPeriodAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedDutyPeriodAllocationStatus.REVOKED -> {
                when (next) {
                    WeightedDutyPeriodAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDutyPeriodAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted duty period allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((ceiling + sequence.toDouble()) * 100) / 100

    fun snapshot(): WeightedDutyPeriodAllocation {
        return WeightedDutyPeriodAllocation(
            code = code,
            label = label,
            ceiling = ceiling,
            priority = priority,
            carrier = carrier,
            segment = segment,
            sequence = sequence,
            tags = tags,
            limit = limit,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted duty period allocation in status requested. */
fun newWeightedDutyPeriodAllocation(code: String, label: String): WeightedDutyPeriodAllocation {
    return WeightedDutyPeriodAllocation(
        code = code,
        label = label,
        ceiling = 0.0,
        priority = 0,
        carrier = "",
        segment = "",
        sequence = 0,
        tags = mutableListOf<String>(),
        limit = 0,
        region = "",
        status = WeightedDutyPeriodAllocationStatus.REQUESTED,
    )
}
