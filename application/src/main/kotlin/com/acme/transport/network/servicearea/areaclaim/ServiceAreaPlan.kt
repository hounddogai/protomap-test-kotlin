package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area plan is in its life cycle. */
enum class ServiceAreaPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a service area plan and its current status. */
data class ServiceAreaPlan(
    var code: String,
    var title: String,
    var budget: Double,
    var rate: Double,
    var channel: String,
    var status: ServiceAreaPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaPlanStatus): Boolean {
        when (status) {
            ServiceAreaPlanStatus.PROPOSED -> return next == ServiceAreaPlanStatus.REJECTED || next == ServiceAreaPlanStatus.WITHDRAWN
            ServiceAreaPlanStatus.APPROVED -> {
                when (next) {
                    ServiceAreaPlanStatus.REJECTED, ServiceAreaPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServiceAreaPlanStatus.REJECTED -> {
                when (next) {
                    ServiceAreaPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaPlanStatus) {
        if (!canMoveTo(next)) {
            error("service area plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round(rate * budget * 100) / 100

    fun snapshot(): ServiceAreaPlan {
        return ServiceAreaPlan(
            code = code,
            title = title,
            budget = budget,
            rate = rate,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a service area plan in status proposed. */
fun newServiceAreaPlan(code: String, title: String): ServiceAreaPlan {
    return ServiceAreaPlan(
        code = code,
        title = title,
        budget = 0.0,
        rate = 0.0,
        channel = "",
        status = ServiceAreaPlanStatus.PROPOSED,
    )
}
