package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.text.normalize

/** Where a service area allocation is in its life cycle. */
enum class ServiceAreaAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One service area allocation, with what the planners need to act on it. */
data class ServiceAreaAllocation(
    var code: String,
    var name: String,
    var lane: String,
    var checksum: String,
    var updatedAt: Int,
    var status: ServiceAreaAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaAllocationStatus): Boolean {
        when (status) {
            ServiceAreaAllocationStatus.DRAFT -> {
                when (next) {
                    ServiceAreaAllocationStatus.ACTIVE, ServiceAreaAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            ServiceAreaAllocationStatus.PENDING -> return next == ServiceAreaAllocationStatus.ACTIVE || next == ServiceAreaAllocationStatus.SUSPENDED
            ServiceAreaAllocationStatus.ACTIVE -> {
                when (next) {
                    ServiceAreaAllocationStatus.SUSPENDED, ServiceAreaAllocationStatus.CLOSED -> return true
                }
                return false
            }
            ServiceAreaAllocationStatus.SUSPENDED -> {
                when (next) {
                    ServiceAreaAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaAllocationStatus) {
        if (!canMoveTo(next)) {
            error("service area allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"service area allocation"} $code"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): ServiceAreaAllocation {
        return ServiceAreaAllocation(
            code = code,
            name = name,
            lane = lane,
            checksum = checksum,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service area allocation in status draft. */
fun newServiceAreaAllocation(code: String, name: String): ServiceAreaAllocation {
    return ServiceAreaAllocation(
        code = code,
        name = name,
        lane = "",
        checksum = "",
        updatedAt = 0,
        status = ServiceAreaAllocationStatus.DRAFT,
    )
}
