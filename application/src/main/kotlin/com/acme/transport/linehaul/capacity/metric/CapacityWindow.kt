package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity window is in its life cycle. */
enum class CapacityWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capacity window as the capacity team keeps it. */
data class CapacityWindow(
    var code: String,
    var name: String,
    var unit: String,
    var depot: String,
    var cost: Double,
    var visible: Boolean,
    var checksum: String,
    var rate: Double,
    var locale: String,
    var weight: Double,
    var minimum: Double,
    var status: CapacityWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityWindowStatus): Boolean {
        when (status) {
            CapacityWindowStatus.DRAFT -> return next == CapacityWindowStatus.PENDING || next == CapacityWindowStatus.SUSPENDED
            CapacityWindowStatus.PENDING -> return next == CapacityWindowStatus.SUSPENDED || next == CapacityWindowStatus.CLOSED
            CapacityWindowStatus.ACTIVE -> {
                when (next) {
                    CapacityWindowStatus.SUSPENDED, CapacityWindowStatus.CLOSED -> return true
                }
                return false
            }
            CapacityWindowStatus.SUSPENDED -> return next == CapacityWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CapacityWindowStatus) {
        if (!canMoveTo(next)) {
            error("capacity window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capacity window"} $code"

    fun adjustedExposure(): Double = round((cost + minimum) * 100) / 100

    fun snapshot(): CapacityWindow {
        return CapacityWindow(
            code = code,
            name = name,
            unit = unit,
            depot = depot,
            cost = cost,
            visible = visible,
            checksum = checksum,
            rate = rate,
            locale = locale,
            weight = weight,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capacity window in status draft. */
fun newCapacityWindow(code: String, name: String): CapacityWindow {
    return CapacityWindow(
        code = code,
        name = name,
        unit = "",
        depot = "",
        cost = 0.0,
        visible = false,
        checksum = "",
        rate = 0.0,
        locale = "",
        weight = 0.0,
        minimum = 0.0,
        status = CapacityWindowStatus.DRAFT,
    )
}
