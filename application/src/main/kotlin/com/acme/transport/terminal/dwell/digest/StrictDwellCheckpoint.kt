package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dwell checkpoint is in its life cycle. */
enum class StrictDwellCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict dwell checkpoint. Its code is unique within a tenant. */
data class StrictDwellCheckpoint(
    var code: String,
    var label: String,
    var currency: String,
    var shiftCode: String,
    var retries: Int,
    var updatedAt: Int,
    var carrier: String,
    var limit: Int,
    var amount: Double,
    var odometer: Int,
    var description: String,
    var status: StrictDwellCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDwellCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDwellCheckpointStatus): Boolean {
        when (status) {
            StrictDwellCheckpointStatus.CREATED -> {
                when (next) {
                    StrictDwellCheckpointStatus.VALIDATED, StrictDwellCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            StrictDwellCheckpointStatus.VALIDATED -> {
                when (next) {
                    StrictDwellCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictDwellCheckpointStatus.PUBLISHED -> {
                when (next) {
                    StrictDwellCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDwellCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("strict dwell checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict dwell checkpoint"} $code"

    fun adjustedLoad(): Double = round((odometer.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): StrictDwellCheckpoint {
        return StrictDwellCheckpoint(
            code = code,
            label = label,
            currency = currency,
            shiftCode = shiftCode,
            retries = retries,
            updatedAt = updatedAt,
            carrier = carrier,
            limit = limit,
            amount = amount,
            odometer = odometer,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict dwell checkpoint in status created. */
fun newStrictDwellCheckpoint(code: String, label: String): StrictDwellCheckpoint {
    return StrictDwellCheckpoint(
        code = code,
        label = label,
        currency = "",
        shiftCode = "",
        retries = 0,
        updatedAt = 0,
        carrier = "",
        limit = 0,
        amount = 0.0,
        odometer = 0,
        description = "",
        status = StrictDwellCheckpointStatus.CREATED,
    )
}
