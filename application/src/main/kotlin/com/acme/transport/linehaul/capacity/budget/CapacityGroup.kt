package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity group is in its life cycle. */
enum class CapacityGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capacity group, with what the planners need to act on it. */
data class CapacityGroup(
    var code: String,
    var label: String,
    var ratio: Double,
    var startsAt: Int,
    var approved: Boolean,
    var budget: Double,
    var status: CapacityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityGroupStatus): Boolean {
        when (status) {
            CapacityGroupStatus.NEW -> return next == CapacityGroupStatus.QUEUED || next == CapacityGroupStatus.FAILED
            CapacityGroupStatus.QUEUED -> return next == CapacityGroupStatus.RUNNING || next == CapacityGroupStatus.FAILED
            CapacityGroupStatus.RUNNING -> return next == CapacityGroupStatus.DONE
            CapacityGroupStatus.DONE -> return next == CapacityGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CapacityGroupStatus) {
        if (!canMoveTo(next)) {
            error("capacity group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capacity group"} $code"

    fun weightedValue(): Double = round(startsAt.toDouble() * budget * 100) / 100

    fun snapshot(): CapacityGroup {
        return CapacityGroup(
            code = code,
            label = label,
            ratio = ratio,
            startsAt = startsAt,
            approved = approved,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity group in status new. */
fun newCapacityGroup(code: String, label: String): CapacityGroup {
    return CapacityGroup(
        code = code,
        label = label,
        ratio = 0.0,
        startsAt = 0,
        approved = false,
        budget = 0.0,
        status = CapacityGroupStatus.NEW,
    )
}
