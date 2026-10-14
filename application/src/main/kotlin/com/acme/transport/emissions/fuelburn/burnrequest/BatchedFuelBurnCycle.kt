package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.text.normalize

/** Where a batched fuel burn cycle is in its life cycle. */
enum class BatchedFuelBurnCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a batched fuel burn cycle and its current status. */
data class BatchedFuelBurnCycle(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var timezone: String,
    var status: BatchedFuelBurnCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelBurnCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelBurnCycleStatus): Boolean {
        when (status) {
            BatchedFuelBurnCycleStatus.IDLE -> return next == BatchedFuelBurnCycleStatus.DRAINING
            BatchedFuelBurnCycleStatus.WARMING -> return next == BatchedFuelBurnCycleStatus.READY || next == BatchedFuelBurnCycleStatus.STOPPED
            BatchedFuelBurnCycleStatus.READY -> return next == BatchedFuelBurnCycleStatus.DRAINING || next == BatchedFuelBurnCycleStatus.STOPPED
            BatchedFuelBurnCycleStatus.DRAINING -> {
                when (next) {
                    BatchedFuelBurnCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFuelBurnCycleStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel burn cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): BatchedFuelBurnCycle {
        return BatchedFuelBurnCycle(
            code = code,
            title = title,
            attributes = attributes,
            durationMinutes = durationMinutes,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched fuel burn cycle in status idle. */
fun newBatchedFuelBurnCycle(code: String, title: String): BatchedFuelBurnCycle {
    return BatchedFuelBurnCycle(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        timezone = "",
        status = BatchedFuelBurnCycleStatus.IDLE,
    )
}
