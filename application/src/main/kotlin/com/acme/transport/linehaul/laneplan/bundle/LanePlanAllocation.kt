package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.text.normalize

/** Where a lane plan allocation is in its life cycle. */
enum class LanePlanAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lane plan allocation and its current status. */
data class LanePlanAllocation(
    var code: String,
    var title: String,
    var label: String,
    var shiftCode: String,
    var createdAt: Int,
    var status: LanePlanAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanAllocationStatus): Boolean {
        when (status) {
            LanePlanAllocationStatus.IDLE -> {
                when (next) {
                    LanePlanAllocationStatus.READY -> return true
                }
                return false
            }
            LanePlanAllocationStatus.WARMING -> return next == LanePlanAllocationStatus.READY || next == LanePlanAllocationStatus.STOPPED
            LanePlanAllocationStatus.READY -> {
                when (next) {
                    LanePlanAllocationStatus.STOPPED -> return true
                }
                return false
            }
            LanePlanAllocationStatus.DRAINING -> {
                when (next) {
                    LanePlanAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lane plan allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): LanePlanAllocation {
        return LanePlanAllocation(
            code = code,
            title = title,
            label = label,
            shiftCode = shiftCode,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan allocation in status idle. */
fun newLanePlanAllocation(code: String, title: String): LanePlanAllocation {
    return LanePlanAllocation(
        code = code,
        title = title,
        label = "",
        shiftCode = "",
        createdAt = 0,
        status = LanePlanAllocationStatus.IDLE,
    )
}
