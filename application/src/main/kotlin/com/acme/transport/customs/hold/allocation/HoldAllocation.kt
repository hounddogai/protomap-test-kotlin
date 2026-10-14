package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold allocation records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold allocation is in its life cycle. */
enum class HoldAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a hold allocation and its current status. */
data class HoldAllocation(
    var code: String,
    var title: String,
    var label: String,
    var lane: String,
    var approved: Boolean,
    var description: String,
    var updatedAt: Int,
    var sequence: Int,
    var segment: String,
    var contactEmail: String,
    var status: HoldAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldAllocationStatus): Boolean {
        when (status) {
            HoldAllocationStatus.NEW -> return next == HoldAllocationStatus.RUNNING
            HoldAllocationStatus.QUEUED -> {
                when (next) {
                    HoldAllocationStatus.RUNNING, HoldAllocationStatus.DONE -> return true
                }
                return false
            }
            HoldAllocationStatus.RUNNING -> return next == HoldAllocationStatus.FAILED
            HoldAllocationStatus.DONE -> {
                when (next) {
                    HoldAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldAllocationStatus) {
        if (!canMoveTo(next)) {
            error("hold allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((updatedAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): HoldAllocation {
        return HoldAllocation(
            code = code,
            title = title,
            label = label,
            lane = lane,
            approved = approved,
            description = description,
            updatedAt = updatedAt,
            sequence = sequence,
            segment = segment,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hold allocation in status new. */
fun newHoldAllocation(code: String, title: String): HoldAllocation {
    return HoldAllocation(
        code = code,
        title = title,
        label = "",
        lane = "",
        approved = false,
        description = "",
        updatedAt = 0,
        sequence = 0,
        segment = "",
        contactEmail = "",
        status = HoldAllocationStatus.NEW,
    )
}
