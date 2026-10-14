package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection batch records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection batch is in its life cycle. */
enum class InspectionBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One inspection batch, with what the planners need to act on it. */
data class InspectionBatch(
    var code: String,
    var name: String,
    var limit: Int,
    var rate: Double,
    var windowDays: Int,
    var reviewer: String,
    var label: String,
    var budget: Double,
    var loadKg: Double,
    var amount: Double,
    var status: InspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBatchStatus): Boolean {
        when (status) {
            InspectionBatchStatus.IDLE -> return next == InspectionBatchStatus.READY || next == InspectionBatchStatus.STOPPED
            InspectionBatchStatus.WARMING -> return next == InspectionBatchStatus.DRAINING || next == InspectionBatchStatus.STOPPED
            InspectionBatchStatus.READY -> return next == InspectionBatchStatus.DRAINING || next == InspectionBatchStatus.STOPPED
            InspectionBatchStatus.DRAINING -> {
                when (next) {
                    InspectionBatchStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("inspection batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun netLoad(): Double = round(rate * budget * 100) / 100

    fun snapshot(): InspectionBatch {
        return InspectionBatch(
            code = code,
            name = name,
            limit = limit,
            rate = rate,
            windowDays = windowDays,
            reviewer = reviewer,
            label = label,
            budget = budget,
            loadKg = loadKg,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection batch in status idle. */
fun newInspectionBatch(code: String, name: String): InspectionBatch {
    return InspectionBatch(
        code = code,
        name = name,
        limit = 0,
        rate = 0.0,
        windowDays = 0,
        reviewer = "",
        label = "",
        budget = 0.0,
        loadKg = 0.0,
        amount = 0.0,
        status = InspectionBatchStatus.IDLE,
    )
}
