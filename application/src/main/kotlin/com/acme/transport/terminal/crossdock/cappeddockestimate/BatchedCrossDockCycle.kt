package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched cross dock cycle is in its life cycle. */
enum class BatchedCrossDockCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched cross dock cycle, with what the planners need to act on it. */
data class BatchedCrossDockCycle(
    var code: String,
    var label: String,
    var threshold: Double,
    var country: String,
    var attempts: Int,
    var region: String,
    var name: String,
    var contactEmail: String,
    var shiftCode: String,
    var status: BatchedCrossDockCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCrossDockCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCrossDockCycleStatus): Boolean {
        when (status) {
            BatchedCrossDockCycleStatus.IDLE -> return next == BatchedCrossDockCycleStatus.WARMING || next == BatchedCrossDockCycleStatus.STOPPED
            BatchedCrossDockCycleStatus.WARMING -> return next == BatchedCrossDockCycleStatus.READY || next == BatchedCrossDockCycleStatus.DRAINING
            BatchedCrossDockCycleStatus.READY -> return next == BatchedCrossDockCycleStatus.STOPPED
            BatchedCrossDockCycleStatus.DRAINING -> {
                when (next) {
                    BatchedCrossDockCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCrossDockCycleStatus) {
        if (!canMoveTo(next)) {
            error("batched cross dock cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round(threshold * attempts.toDouble() * 100) / 100

    fun snapshot(): BatchedCrossDockCycle {
        return BatchedCrossDockCycle(
            code = code,
            label = label,
            threshold = threshold,
            country = country,
            attempts = attempts,
            region = region,
            name = name,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched cross dock cycle in status idle. */
fun newBatchedCrossDockCycle(code: String, label: String): BatchedCrossDockCycle {
    return BatchedCrossDockCycle(
        code = code,
        label = label,
        threshold = 0.0,
        country = "",
        attempts = 0,
        region = "",
        name = "",
        contactEmail = "",
        shiftCode = "",
        status = BatchedCrossDockCycleStatus.IDLE,
    )
}
