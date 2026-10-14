package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period allocation is in its life cycle. */
enum class DutyPeriodAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A duty period allocation. Its code is unique within a tenant. */
data class DutyPeriodAllocation(
    var code: String,
    var name: String,
    var version: Int,
    var category: String,
    var label: String,
    var expiresAt: Int,
    var capacity: Int,
    var startsAt: Int,
    var driverId: String,
    var fuelLiters: Double,
    var status: DutyPeriodAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodAllocationStatus): Boolean {
        when (status) {
            DutyPeriodAllocationStatus.DRAFT -> {
                when (next) {
                    DutyPeriodAllocationStatus.PENDING, DutyPeriodAllocationStatus.CLOSED -> return true
                }
                return false
            }
            DutyPeriodAllocationStatus.PENDING -> {
                when (next) {
                    DutyPeriodAllocationStatus.ACTIVE -> return true
                }
                return false
            }
            DutyPeriodAllocationStatus.ACTIVE -> return next == DutyPeriodAllocationStatus.CLOSED
            DutyPeriodAllocationStatus.SUSPENDED -> return next == DutyPeriodAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DutyPeriodAllocationStatus) {
        if (!canMoveTo(next)) {
            error("duty period allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"duty period allocation"} $code"

    fun weightedValue(): Double = round((fuelLiters + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodAllocation {
        return DutyPeriodAllocation(
            code = code,
            name = name,
            version = version,
            category = category,
            label = label,
            expiresAt = expiresAt,
            capacity = capacity,
            startsAt = startsAt,
            driverId = driverId,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a duty period allocation in status draft. */
fun newDutyPeriodAllocation(code: String, name: String): DutyPeriodAllocation {
    return DutyPeriodAllocation(
        code = code,
        name = name,
        version = 0,
        category = "",
        label = "",
        expiresAt = 0,
        capacity = 0,
        startsAt = 0,
        driverId = "",
        fuelLiters = 0.0,
        status = DutyPeriodAllocationStatus.DRAFT,
    )
}
