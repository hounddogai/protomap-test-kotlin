package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan preference is in its life cycle. */
enum class ServicePlanPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a service plan preference and its current status. */
data class ServicePlanPreference(
    var code: String,
    var name: String,
    var summary: String,
    var category: String,
    var etaMinutes: Int,
    var source: String,
    var pallets: Int,
    var reviewer: String,
    var capacity: Int,
    var title: String,
    var status: ServicePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanPreferenceStatus): Boolean {
        when (status) {
            ServicePlanPreferenceStatus.IDLE -> {
                when (next) {
                    ServicePlanPreferenceStatus.WARMING, ServicePlanPreferenceStatus.DRAINING -> return true
                }
                return false
            }
            ServicePlanPreferenceStatus.WARMING -> {
                when (next) {
                    ServicePlanPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            ServicePlanPreferenceStatus.READY -> {
                when (next) {
                    ServicePlanPreferenceStatus.DRAINING, ServicePlanPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            ServicePlanPreferenceStatus.DRAINING -> return next == ServicePlanPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServicePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("service plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round(pallets.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ServicePlanPreference {
        return ServicePlanPreference(
            code = code,
            name = name,
            summary = summary,
            category = category,
            etaMinutes = etaMinutes,
            source = source,
            pallets = pallets,
            reviewer = reviewer,
            capacity = capacity,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a service plan preference in status idle. */
fun newServicePlanPreference(code: String, name: String): ServicePlanPreference {
    return ServicePlanPreference(
        code = code,
        name = name,
        summary = "",
        category = "",
        etaMinutes = 0,
        source = "",
        pallets = 0,
        reviewer = "",
        capacity = 0,
        title = "",
        status = ServicePlanPreferenceStatus.IDLE,
    )
}
