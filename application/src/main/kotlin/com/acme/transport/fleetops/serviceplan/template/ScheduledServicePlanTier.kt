package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled service plan tier is in its life cycle. */
enum class ScheduledServicePlanTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled service plan tier as the service plan team keeps it. */
data class ScheduledServicePlanTier(
    var code: String,
    var title: String,
    var summary: String,
    var limit: Int,
    var quantity: Int,
    var depot: String,
    var reviewer: String,
    var status: ScheduledServicePlanTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServicePlanTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServicePlanTierStatus): Boolean {
        when (status) {
            ScheduledServicePlanTierStatus.PLANNED -> return next == ScheduledServicePlanTierStatus.COMPLETED || next == ScheduledServicePlanTierStatus.CANCELLED
            ScheduledServicePlanTierStatus.SCHEDULED -> {
                when (next) {
                    ScheduledServicePlanTierStatus.IN_PROGRESS, ScheduledServicePlanTierStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledServicePlanTierStatus.IN_PROGRESS -> return next == ScheduledServicePlanTierStatus.COMPLETED
            ScheduledServicePlanTierStatus.COMPLETED -> {
                when (next) {
                    ScheduledServicePlanTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledServicePlanTierStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service plan tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled service plan tier"} $code"

    fun projectedLoad(): Double = round(quantity.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): ScheduledServicePlanTier {
        return ScheduledServicePlanTier(
            code = code,
            title = title,
            summary = summary,
            limit = limit,
            quantity = quantity,
            depot = depot,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled service plan tier in status planned. */
fun newScheduledServicePlanTier(code: String, title: String): ScheduledServicePlanTier {
    return ScheduledServicePlanTier(
        code = code,
        title = title,
        summary = "",
        limit = 0,
        quantity = 0,
        depot = "",
        reviewer = "",
        status = ScheduledServicePlanTierStatus.PLANNED,
    )
}
