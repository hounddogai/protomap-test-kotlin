package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation batch is in its life cycle. */
enum class ViolationBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a violation batch and its current status. */
data class ViolationBatch(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var windowDays: Int,
    var vehicleId: String,
    var notes: String,
    var maximum: Double,
    var approved: Boolean,
    var status: ViolationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationBatchStatus): Boolean {
        when (status) {
            ViolationBatchStatus.REQUESTED -> return next == ViolationBatchStatus.GRANTED || next == ViolationBatchStatus.REVOKED
            ViolationBatchStatus.GRANTED -> {
                when (next) {
                    ViolationBatchStatus.REVOKED -> return true
                }
                return false
            }
            ViolationBatchStatus.REVOKED -> return next == ViolationBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationBatchStatus) {
        if (!canMoveTo(next)) {
            error("violation batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"violation batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round(maximum * windowDays.toDouble() * 100) / 100

    fun snapshot(): ViolationBatch {
        return ViolationBatch(
            code = code,
            label = label,
            attributes = attributes,
            windowDays = windowDays,
            vehicleId = vehicleId,
            notes = notes,
            maximum = maximum,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a violation batch in status requested. */
fun newViolationBatch(code: String, label: String): ViolationBatch {
    return ViolationBatch(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        windowDays = 0,
        vehicleId = "",
        notes = "",
        maximum = 0.0,
        approved = false,
        status = ViolationBatchStatus.REQUESTED,
    )
}
