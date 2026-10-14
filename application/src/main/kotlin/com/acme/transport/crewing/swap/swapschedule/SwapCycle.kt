package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap cycle is in its life cycle. */
enum class SwapCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a swap cycle and its current status. */
data class SwapCycle(
    var code: String,
    var label: String,
    var routeCode: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var batchSize: Int,
    var approved: Boolean,
    var capacity: Int,
    var notes: String,
    var status: SwapCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapCycleStatus): Boolean {
        when (status) {
            SwapCycleStatus.DRAFT -> {
                when (next) {
                    SwapCycleStatus.ACTIVE -> return true
                }
                return false
            }
            SwapCycleStatus.PENDING -> {
                when (next) {
                    SwapCycleStatus.CLOSED -> return true
                }
                return false
            }
            SwapCycleStatus.ACTIVE -> {
                when (next) {
                    SwapCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            SwapCycleStatus.SUSPENDED -> return next == SwapCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SwapCycleStatus) {
        if (!canMoveTo(next)) {
            error("swap cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(batchSize.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): SwapCycle {
        return SwapCycle(
            code = code,
            label = label,
            routeCode = routeCode,
            pallets = pallets,
            tags = tags,
            batchSize = batchSize,
            approved = approved,
            capacity = capacity,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap cycle in status draft. */
fun newSwapCycle(code: String, label: String): SwapCycle {
    return SwapCycle(
        code = code,
        label = label,
        routeCode = "",
        pallets = 0,
        tags = mutableListOf<String>(),
        batchSize = 0,
        approved = false,
        capacity = 0,
        notes = "",
        status = SwapCycleStatus.DRAFT,
    )
}
