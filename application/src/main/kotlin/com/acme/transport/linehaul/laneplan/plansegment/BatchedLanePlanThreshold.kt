package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched lane plan threshold is in its life cycle. */
enum class BatchedLanePlanThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched lane plan threshold as the lane plan team keeps it. */
data class BatchedLanePlanThreshold(
    var code: String,
    var name: String,
    var attempts: Int,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var vehicleId: String,
    var category: String,
    var status: BatchedLanePlanThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLanePlanThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLanePlanThresholdStatus): Boolean {
        when (status) {
            BatchedLanePlanThresholdStatus.PROPOSED -> {
                when (next) {
                    BatchedLanePlanThresholdStatus.REJECTED, BatchedLanePlanThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedLanePlanThresholdStatus.APPROVED -> return next == BatchedLanePlanThresholdStatus.REJECTED
            BatchedLanePlanThresholdStatus.REJECTED -> return next == BatchedLanePlanThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedLanePlanThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched lane plan threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((attempts.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): BatchedLanePlanThreshold {
        return BatchedLanePlanThreshold(
            code = code,
            name = name,
            attempts = attempts,
            attributes = attributes,
            pallets = pallets,
            vehicleId = vehicleId,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched lane plan threshold in status proposed. */
fun newBatchedLanePlanThreshold(code: String, name: String): BatchedLanePlanThreshold {
    return BatchedLanePlanThreshold(
        code = code,
        name = name,
        attempts = 0,
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        vehicleId = "",
        category = "",
        status = BatchedLanePlanThresholdStatus.PROPOSED,
    )
}
