package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual capacity exception is in its life cycle. */
enum class ManualCapacityExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual capacity exception as the capacity team keeps it. */
data class ManualCapacityException(
    var code: String,
    var label: String,
    var revision: Int,
    var stopCount: Int,
    var lane: String,
    var status: ManualCapacityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCapacityExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCapacityExceptionStatus): Boolean {
        when (status) {
            ManualCapacityExceptionStatus.PLANNED -> {
                when (next) {
                    ManualCapacityExceptionStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualCapacityExceptionStatus.SCHEDULED -> return next == ManualCapacityExceptionStatus.COMPLETED
            ManualCapacityExceptionStatus.IN_PROGRESS -> return next == ManualCapacityExceptionStatus.COMPLETED || next == ManualCapacityExceptionStatus.CANCELLED
            ManualCapacityExceptionStatus.COMPLETED -> {
                when (next) {
                    ManualCapacityExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCapacityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("manual capacity exception $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((revision.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ManualCapacityException {
        return ManualCapacityException(
            code = code,
            label = label,
            revision = revision,
            stopCount = stopCount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual capacity exception in status planned. */
fun newManualCapacityException(code: String, label: String): ManualCapacityException {
    return ManualCapacityException(
        code = code,
        label = label,
        revision = 0,
        stopCount = 0,
        lane = "",
        status = ManualCapacityExceptionStatus.PLANNED,
    )
}
