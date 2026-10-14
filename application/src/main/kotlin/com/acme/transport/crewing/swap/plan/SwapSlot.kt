package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap slot is in its life cycle. */
enum class SwapSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a swap slot and its current status. */
data class SwapSlot(
    var code: String,
    var title: String,
    var ownerId: String,
    var createdAt: Int,
    var revision: Int,
    var label: String,
    var status: SwapSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapSlotStatus): Boolean {
        when (status) {
            SwapSlotStatus.OPEN -> return next == SwapSlotStatus.ARCHIVED
            SwapSlotStatus.IN_REVIEW -> return next == SwapSlotStatus.REOPENED
            SwapSlotStatus.RESOLVED -> return next == SwapSlotStatus.REOPENED || next == SwapSlotStatus.ARCHIVED
            SwapSlotStatus.REOPENED -> {
                when (next) {
                    SwapSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapSlotStatus) {
        if (!canMoveTo(next)) {
            error("swap slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"swap slot"} $code"

    fun adjustedVolume(): Double = round(revision.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): SwapSlot {
        return SwapSlot(
            code = code,
            title = title,
            ownerId = ownerId,
            createdAt = createdAt,
            revision = revision,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap slot in status open. */
fun newSwapSlot(code: String, title: String): SwapSlot {
    return SwapSlot(
        code = code,
        title = title,
        ownerId = "",
        createdAt = 0,
        revision = 0,
        label = "",
        status = SwapSlotStatus.OPEN,
    )
}
