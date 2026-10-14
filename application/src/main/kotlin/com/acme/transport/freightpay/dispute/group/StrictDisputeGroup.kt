package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict dispute group records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a strict dispute group is in its life cycle. */
enum class StrictDisputeGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict dispute group, with what the planners need to act on it. */
data class StrictDisputeGroup(
    var code: String,
    var name: String,
    var shiftCode: String,
    var revision: Int,
    var ceiling: Double,
    var lane: String,
    var attributes: MutableMap<String, String>,
    var batchSize: Int,
    var description: String,
    var status: StrictDisputeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDisputeGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDisputeGroupStatus): Boolean {
        when (status) {
            StrictDisputeGroupStatus.IDLE -> {
                when (next) {
                    StrictDisputeGroupStatus.READY -> return true
                }
                return false
            }
            StrictDisputeGroupStatus.WARMING -> return next == StrictDisputeGroupStatus.READY
            StrictDisputeGroupStatus.READY -> return next == StrictDisputeGroupStatus.DRAINING || next == StrictDisputeGroupStatus.STOPPED
            StrictDisputeGroupStatus.DRAINING -> return next == StrictDisputeGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictDisputeGroupStatus) {
        if (!canMoveTo(next)) {
            error("strict dispute group $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round(ceiling * revision.toDouble() * 100) / 100

    fun snapshot(): StrictDisputeGroup {
        return StrictDisputeGroup(
            code = code,
            name = name,
            shiftCode = shiftCode,
            revision = revision,
            ceiling = ceiling,
            lane = lane,
            attributes = attributes,
            batchSize = batchSize,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict dispute group in status idle. */
fun newStrictDisputeGroup(code: String, name: String): StrictDisputeGroup {
    return StrictDisputeGroup(
        code = code,
        name = name,
        shiftCode = "",
        revision = 0,
        ceiling = 0.0,
        lane = "",
        attributes = mutableMapOf<String, String>(),
        batchSize = 0,
        description = "",
        status = StrictDisputeGroupStatus.IDLE,
    )
}
