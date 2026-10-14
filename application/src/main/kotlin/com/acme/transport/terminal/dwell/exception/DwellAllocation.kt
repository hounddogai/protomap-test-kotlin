package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell allocation is in its life cycle. */
enum class DwellAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a dwell allocation and its current status. */
data class DwellAllocation(
    var code: String,
    var label: String,
    var ceiling: Double,
    var etaMinutes: Int,
    var retries: Int,
    var expiresAt: Int,
    var ownerId: String,
    var currency: String,
    var rate: Double,
    var quantity: Int,
    var status: DwellAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellAllocationStatus): Boolean {
        when (status) {
            DwellAllocationStatus.PLANNED -> return next == DwellAllocationStatus.IN_PROGRESS
            DwellAllocationStatus.SCHEDULED -> return next == DwellAllocationStatus.IN_PROGRESS || next == DwellAllocationStatus.CANCELLED
            DwellAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    DwellAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            DwellAllocationStatus.COMPLETED -> {
                when (next) {
                    DwellAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellAllocationStatus) {
        if (!canMoveTo(next)) {
            error("dwell allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round((retries.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DwellAllocation {
        return DwellAllocation(
            code = code,
            label = label,
            ceiling = ceiling,
            etaMinutes = etaMinutes,
            retries = retries,
            expiresAt = expiresAt,
            ownerId = ownerId,
            currency = currency,
            rate = rate,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell allocation in status planned. */
fun newDwellAllocation(code: String, label: String): DwellAllocation {
    return DwellAllocation(
        code = code,
        label = label,
        ceiling = 0.0,
        etaMinutes = 0,
        retries = 0,
        expiresAt = 0,
        ownerId = "",
        currency = "",
        rate = 0.0,
        quantity = 0,
        status = DwellAllocationStatus.PLANNED,
    )
}
