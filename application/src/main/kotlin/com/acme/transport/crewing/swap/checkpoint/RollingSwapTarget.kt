package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a rolling swap target is in its life cycle. */
enum class RollingSwapTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling swap target and its current status. */
data class RollingSwapTarget(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var routeCode: String,
    var archived: Boolean,
    var checksum: String,
    var reviewer: String,
    var carrier: String,
    var status: RollingSwapTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapTargetStatus): Boolean {
        when (status) {
            RollingSwapTargetStatus.IDLE -> {
                when (next) {
                    RollingSwapTargetStatus.WARMING, RollingSwapTargetStatus.DRAINING -> return true
                }
                return false
            }
            RollingSwapTargetStatus.WARMING -> return next == RollingSwapTargetStatus.READY
            RollingSwapTargetStatus.READY -> return next == RollingSwapTargetStatus.DRAINING
            RollingSwapTargetStatus.DRAINING -> {
                when (next) {
                    RollingSwapTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingSwapTargetStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): RollingSwapTarget {
        return RollingSwapTarget(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            archived = archived,
            checksum = checksum,
            reviewer = reviewer,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling swap target in status idle. */
fun newRollingSwapTarget(code: String, name: String): RollingSwapTarget {
    return RollingSwapTarget(
        code = code,
        name = name,
        etaMinutes = 0,
        routeCode = "",
        archived = false,
        checksum = "",
        reviewer = "",
        carrier = "",
        status = RollingSwapTargetStatus.IDLE,
    )
}
