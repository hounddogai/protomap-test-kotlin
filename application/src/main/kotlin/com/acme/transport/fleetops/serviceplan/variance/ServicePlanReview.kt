package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan review is in its life cycle. */
enum class ServicePlanReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A service plan review. Its code is unique within a tenant. */
data class ServicePlanReview(
    var code: String,
    var label: String,
    var region: String,
    var endsAt: Int,
    var capacity: Int,
    var status: ServicePlanReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanReviewStatus): Boolean {
        when (status) {
            ServicePlanReviewStatus.PLANNED -> {
                when (next) {
                    ServicePlanReviewStatus.SCHEDULED, ServicePlanReviewStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ServicePlanReviewStatus.SCHEDULED -> {
                when (next) {
                    ServicePlanReviewStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ServicePlanReviewStatus.IN_PROGRESS -> return next == ServicePlanReviewStatus.COMPLETED || next == ServicePlanReviewStatus.CANCELLED
            ServicePlanReviewStatus.COMPLETED -> {
                when (next) {
                    ServicePlanReviewStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanReviewStatus) {
        if (!canMoveTo(next)) {
            error("service plan review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round(endsAt.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ServicePlanReview {
        return ServicePlanReview(
            code = code,
            label = label,
            region = region,
            endsAt = endsAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a service plan review in status planned. */
fun newServicePlanReview(code: String, label: String): ServicePlanReview {
    return ServicePlanReview(
        code = code,
        label = label,
        region = "",
        endsAt = 0,
        capacity = 0,
        status = ServicePlanReviewStatus.PLANNED,
    )
}
