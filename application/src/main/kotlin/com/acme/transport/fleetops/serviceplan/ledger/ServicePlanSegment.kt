package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan segment is in its life cycle. */
enum class ServicePlanSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service plan segment. Its code is unique within a tenant. */
data class ServicePlanSegment(
    var code: String,
    var title: String,
    var notes: String,
    var cost: Double,
    var graceDays: Int,
    var driverId: String,
    var archived: Boolean,
    var status: ServicePlanSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanSegmentStatus): Boolean {
        when (status) {
            ServicePlanSegmentStatus.NEW -> return next == ServicePlanSegmentStatus.QUEUED || next == ServicePlanSegmentStatus.FAILED
            ServicePlanSegmentStatus.QUEUED -> {
                when (next) {
                    ServicePlanSegmentStatus.RUNNING, ServicePlanSegmentStatus.FAILED -> return true
                }
                return false
            }
            ServicePlanSegmentStatus.RUNNING -> {
                when (next) {
                    ServicePlanSegmentStatus.DONE, ServicePlanSegmentStatus.FAILED -> return true
                }
                return false
            }
            ServicePlanSegmentStatus.DONE -> {
                when (next) {
                    ServicePlanSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanSegmentStatus) {
        if (!canMoveTo(next)) {
            error("service plan segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service plan segment"} $code"

    fun netValue(): Double = round(cost * graceDays.toDouble() * 100) / 100

    fun snapshot(): ServicePlanSegment {
        return ServicePlanSegment(
            code = code,
            title = title,
            notes = notes,
            cost = cost,
            graceDays = graceDays,
            driverId = driverId,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service plan segment in status new. */
fun newServicePlanSegment(code: String, title: String): ServicePlanSegment {
    return ServicePlanSegment(
        code = code,
        title = title,
        notes = "",
        cost = 0.0,
        graceDays = 0,
        driverId = "",
        archived = false,
        status = ServicePlanSegmentStatus.NEW,
    )
}
