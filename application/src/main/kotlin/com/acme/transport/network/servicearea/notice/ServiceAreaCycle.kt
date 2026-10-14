package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area cycle is in its life cycle. */
enum class ServiceAreaCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a service area cycle and its current status. */
data class ServiceAreaCycle(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var dock: String,
    var ownerId: String,
    var trailerId: String,
    var endsAt: Int,
    var status: ServiceAreaCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaCycleStatus): Boolean {
        when (status) {
            ServiceAreaCycleStatus.PLANNED -> return next == ServiceAreaCycleStatus.CANCELLED
            ServiceAreaCycleStatus.SCHEDULED -> return next == ServiceAreaCycleStatus.IN_PROGRESS || next == ServiceAreaCycleStatus.CANCELLED
            ServiceAreaCycleStatus.IN_PROGRESS -> {
                when (next) {
                    ServiceAreaCycleStatus.COMPLETED, ServiceAreaCycleStatus.CANCELLED -> return true
                }
                return false
            }
            ServiceAreaCycleStatus.COMPLETED -> return next == ServiceAreaCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServiceAreaCycleStatus) {
        if (!canMoveTo(next)) {
            error("service area cycle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaCycle {
        return ServiceAreaCycle(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            dock = dock,
            ownerId = ownerId,
            trailerId = trailerId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area cycle in status planned. */
fun newServiceAreaCycle(code: String, name: String): ServiceAreaCycle {
    return ServiceAreaCycle(
        code = code,
        name = name,
        etaMinutes = 0,
        dock = "",
        ownerId = "",
        trailerId = "",
        endsAt = 0,
        status = ServiceAreaCycleStatus.PLANNED,
    )
}
