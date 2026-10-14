package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled accessorial batch is in its life cycle. */
enum class ScheduledAccessorialBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled accessorial batch. Its code is unique within a tenant. */
data class ScheduledAccessorialBatch(
    var code: String,
    var name: String,
    var carrier: String,
    var revision: Int,
    var capacity: Int,
    var minimum: Double,
    var odometer: Int,
    var status: ScheduledAccessorialBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAccessorialBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAccessorialBatchStatus): Boolean {
        when (status) {
            ScheduledAccessorialBatchStatus.PLANNED -> {
                when (next) {
                    ScheduledAccessorialBatchStatus.COMPLETED, ScheduledAccessorialBatchStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledAccessorialBatchStatus.SCHEDULED -> return next == ScheduledAccessorialBatchStatus.COMPLETED
            ScheduledAccessorialBatchStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledAccessorialBatchStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledAccessorialBatchStatus.COMPLETED -> {
                when (next) {
                    ScheduledAccessorialBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledAccessorialBatchStatus) {
        if (!canMoveTo(next)) {
            error("scheduled accessorial batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round(odometer.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ScheduledAccessorialBatch {
        return ScheduledAccessorialBatch(
            code = code,
            name = name,
            carrier = carrier,
            revision = revision,
            capacity = capacity,
            minimum = minimum,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled accessorial batch in status planned. */
fun newScheduledAccessorialBatch(code: String, name: String): ScheduledAccessorialBatch {
    return ScheduledAccessorialBatch(
        code = code,
        name = name,
        carrier = "",
        revision = 0,
        capacity = 0,
        minimum = 0.0,
        odometer = 0,
        status = ScheduledAccessorialBatchStatus.PLANNED,
    )
}
