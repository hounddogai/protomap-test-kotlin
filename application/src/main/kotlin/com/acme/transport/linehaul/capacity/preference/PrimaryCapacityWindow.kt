package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary capacity window is in its life cycle. */
enum class PrimaryCapacityWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary capacity window and its current status. */
data class PrimaryCapacityWindow(
    var code: String,
    var title: String,
    var description: String,
    var retries: Int,
    var segment: String,
    var quantity: Int,
    var status: PrimaryCapacityWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCapacityWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCapacityWindowStatus): Boolean {
        when (status) {
            PrimaryCapacityWindowStatus.PLANNED -> {
                when (next) {
                    PrimaryCapacityWindowStatus.SCHEDULED -> return true
                }
                return false
            }
            PrimaryCapacityWindowStatus.SCHEDULED -> return next == PrimaryCapacityWindowStatus.IN_PROGRESS || next == PrimaryCapacityWindowStatus.CANCELLED
            PrimaryCapacityWindowStatus.IN_PROGRESS -> return next == PrimaryCapacityWindowStatus.COMPLETED
            PrimaryCapacityWindowStatus.COMPLETED -> {
                when (next) {
                    PrimaryCapacityWindowStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCapacityWindowStatus) {
        if (!canMoveTo(next)) {
            error("primary capacity window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round(retries.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): PrimaryCapacityWindow {
        return PrimaryCapacityWindow(
            code = code,
            title = title,
            description = description,
            retries = retries,
            segment = segment,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary capacity window in status planned. */
fun newPrimaryCapacityWindow(code: String, title: String): PrimaryCapacityWindow {
    return PrimaryCapacityWindow(
        code = code,
        title = title,
        description = "",
        retries = 0,
        segment = "",
        quantity = 0,
        status = PrimaryCapacityWindowStatus.PLANNED,
    )
}
