package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan draft is in its life cycle. */
enum class ServicePlanDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A service plan draft as the service plan team keeps it. */
data class ServicePlanDraft(
    var code: String,
    var label: String,
    var pallets: Int,
    var routeCode: String,
    var checksum: String,
    var distanceKm: Double,
    var updatedAt: Int,
    var timezone: String,
    var segment: String,
    var maximum: Double,
    var batchSize: Int,
    var status: ServicePlanDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanDraftStatus): Boolean {
        when (status) {
            ServicePlanDraftStatus.PLANNED -> return next == ServicePlanDraftStatus.CANCELLED
            ServicePlanDraftStatus.SCHEDULED -> {
                when (next) {
                    ServicePlanDraftStatus.IN_PROGRESS, ServicePlanDraftStatus.COMPLETED -> return true
                }
                return false
            }
            ServicePlanDraftStatus.IN_PROGRESS -> return next == ServicePlanDraftStatus.COMPLETED || next == ServicePlanDraftStatus.CANCELLED
            ServicePlanDraftStatus.COMPLETED -> {
                when (next) {
                    ServicePlanDraftStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanDraftStatus) {
        if (!canMoveTo(next)) {
            error("service plan draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"service plan draft"} $code"

    fun weightedTotal(): Double = round((maximum + pallets.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanDraft {
        return ServicePlanDraft(
            code = code,
            label = label,
            pallets = pallets,
            routeCode = routeCode,
            checksum = checksum,
            distanceKm = distanceKm,
            updatedAt = updatedAt,
            timezone = timezone,
            segment = segment,
            maximum = maximum,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a service plan draft in status planned. */
fun newServicePlanDraft(code: String, label: String): ServicePlanDraft {
    return ServicePlanDraft(
        code = code,
        label = label,
        pallets = 0,
        routeCode = "",
        checksum = "",
        distanceKm = 0.0,
        updatedAt = 0,
        timezone = "",
        segment = "",
        maximum = 0.0,
        batchSize = 0,
        status = ServicePlanDraftStatus.PLANNED,
    )
}
