package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan summary records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan summary is in its life cycle. */
enum class ServicePlanSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service plan summary. Its code is unique within a tenant. */
data class ServicePlanSummary(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var ceiling: Double,
    var notes: String,
    var phone: String,
    var summary: String,
    var loadKg: Double,
    var status: ServicePlanSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanSummaryStatus): Boolean {
        when (status) {
            ServicePlanSummaryStatus.IDLE -> {
                when (next) {
                    ServicePlanSummaryStatus.WARMING, ServicePlanSummaryStatus.DRAINING -> return true
                }
                return false
            }
            ServicePlanSummaryStatus.WARMING -> return next == ServicePlanSummaryStatus.DRAINING
            ServicePlanSummaryStatus.READY -> {
                when (next) {
                    ServicePlanSummaryStatus.STOPPED -> return true
                }
                return false
            }
            ServicePlanSummaryStatus.DRAINING -> return next == ServicePlanSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServicePlanSummaryStatus) {
        if (!canMoveTo(next)) {
            error("service plan summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service plan summary"} $code"

    fun netLoad(): Double = round((loadKg + ceiling) * 100) / 100

    fun snapshot(): ServicePlanSummary {
        return ServicePlanSummary(
            code = code,
            name = name,
            pinned = pinned,
            ceiling = ceiling,
            notes = notes,
            phone = phone,
            summary = summary,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan summary in status idle. */
fun newServicePlanSummary(code: String, name: String): ServicePlanSummary {
    return ServicePlanSummary(
        code = code,
        name = name,
        pinned = false,
        ceiling = 0.0,
        notes = "",
        phone = "",
        summary = "",
        loadKg = 0.0,
        status = ServicePlanSummaryStatus.IDLE,
    )
}
