package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation allocation is in its life cycle. */
enum class SortationAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One sortation allocation, with what the planners need to act on it. */
data class SortationAllocation(
    var code: String,
    var name: String,
    var margin: Double,
    var locale: String,
    var active: Boolean,
    var visible: Boolean,
    var currency: String,
    var ratio: Double,
    var status: SortationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationAllocationStatus): Boolean {
        when (status) {
            SortationAllocationStatus.PLANNED -> return next == SortationAllocationStatus.IN_PROGRESS || next == SortationAllocationStatus.COMPLETED
            SortationAllocationStatus.SCHEDULED -> return next == SortationAllocationStatus.IN_PROGRESS || next == SortationAllocationStatus.COMPLETED
            SortationAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    SortationAllocationStatus.COMPLETED -> return true
                }
                return false
            }
            SortationAllocationStatus.COMPLETED -> {
                when (next) {
                    SortationAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("sortation allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"sortation allocation"} $code"

    fun projectedExposure(): Double = round(margin * ratio * 100) / 100

    fun snapshot(): SortationAllocation {
        return SortationAllocation(
            code = code,
            name = name,
            margin = margin,
            locale = locale,
            active = active,
            visible = visible,
            currency = currency,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a sortation allocation in status planned. */
fun newSortationAllocation(code: String, name: String): SortationAllocation {
    return SortationAllocation(
        code = code,
        name = name,
        margin = 0.0,
        locale = "",
        active = false,
        visible = false,
        currency = "",
        ratio = 0.0,
        status = SortationAllocationStatus.PLANNED,
    )
}
