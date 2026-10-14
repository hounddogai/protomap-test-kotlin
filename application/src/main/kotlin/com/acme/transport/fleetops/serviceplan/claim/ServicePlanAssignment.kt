package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan assignment is in its life cycle. */
enum class ServicePlanAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service plan assignment as the service plan team keeps it. */
data class ServicePlanAssignment(
    var code: String,
    var title: String,
    var segment: String,
    var description: String,
    var score: Double,
    var attempts: Int,
    var dock: String,
    var driverId: String,
    var visible: Boolean,
    var windowDays: Int,
    var lane: String,
    var status: ServicePlanAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanAssignmentStatus): Boolean {
        when (status) {
            ServicePlanAssignmentStatus.REQUESTED -> {
                when (next) {
                    ServicePlanAssignmentStatus.REVOKED, ServicePlanAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanAssignmentStatus.GRANTED -> {
                when (next) {
                    ServicePlanAssignmentStatus.REVOKED, ServicePlanAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanAssignmentStatus.REVOKED -> {
                when (next) {
                    ServicePlanAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("service plan assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round(score * windowDays.toDouble() * 100) / 100

    fun snapshot(): ServicePlanAssignment {
        return ServicePlanAssignment(
            code = code,
            title = title,
            segment = segment,
            description = description,
            score = score,
            attempts = attempts,
            dock = dock,
            driverId = driverId,
            visible = visible,
            windowDays = windowDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a service plan assignment in status requested. */
fun newServicePlanAssignment(code: String, title: String): ServicePlanAssignment {
    return ServicePlanAssignment(
        code = code,
        title = title,
        segment = "",
        description = "",
        score = 0.0,
        attempts = 0,
        dock = "",
        driverId = "",
        visible = false,
        windowDays = 0,
        lane = "",
        status = ServicePlanAssignmentStatus.REQUESTED,
    )
}
