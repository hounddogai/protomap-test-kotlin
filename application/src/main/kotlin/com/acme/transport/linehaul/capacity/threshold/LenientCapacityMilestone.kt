package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient capacity milestone is in its life cycle. */
enum class LenientCapacityMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient capacity milestone, with what the planners need to act on it. */
data class LenientCapacityMilestone(
    var code: String,
    var label: String,
    var active: Boolean,
    var title: String,
    var createdAt: Int,
    var notes: String,
    var source: String,
    var durationMinutes: Int,
    var floorValue: Double,
    var sequence: Int,
    var limit: Int,
    var status: LenientCapacityMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCapacityMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCapacityMilestoneStatus): Boolean {
        when (status) {
            LenientCapacityMilestoneStatus.CREATED -> return next == LenientCapacityMilestoneStatus.RETIRED
            LenientCapacityMilestoneStatus.VALIDATED -> {
                when (next) {
                    LenientCapacityMilestoneStatus.PUBLISHED, LenientCapacityMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            LenientCapacityMilestoneStatus.PUBLISHED -> return next == LenientCapacityMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientCapacityMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("lenient capacity milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((durationMinutes.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): LenientCapacityMilestone {
        return LenientCapacityMilestone(
            code = code,
            label = label,
            active = active,
            title = title,
            createdAt = createdAt,
            notes = notes,
            source = source,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            sequence = sequence,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient capacity milestone in status created. */
fun newLenientCapacityMilestone(code: String, label: String): LenientCapacityMilestone {
    return LenientCapacityMilestone(
        code = code,
        label = label,
        active = false,
        title = "",
        createdAt = 0,
        notes = "",
        source = "",
        durationMinutes = 0,
        floorValue = 0.0,
        sequence = 0,
        limit = 0,
        status = LenientCapacityMilestoneStatus.CREATED,
    )
}
