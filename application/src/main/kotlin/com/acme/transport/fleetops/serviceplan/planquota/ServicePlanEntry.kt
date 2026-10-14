package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan entry is in its life cycle. */
enum class ServicePlanEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a service plan entry and its current status. */
data class ServicePlanEntry(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var revision: Int,
    var currency: String,
    var lane: String,
    var externalRef: String,
    var margin: Double,
    var phone: String,
    var status: ServicePlanEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanEntryStatus): Boolean {
        when (status) {
            ServicePlanEntryStatus.OPEN -> {
                when (next) {
                    ServicePlanEntryStatus.IN_REVIEW, ServicePlanEntryStatus.RESOLVED -> return true
                }
                return false
            }
            ServicePlanEntryStatus.IN_REVIEW -> return next == ServicePlanEntryStatus.RESOLVED || next == ServicePlanEntryStatus.REOPENED
            ServicePlanEntryStatus.RESOLVED -> return next == ServicePlanEntryStatus.REOPENED
            ServicePlanEntryStatus.REOPENED -> return next == ServicePlanEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ServicePlanEntryStatus) {
        if (!canMoveTo(next)) {
            error("service plan entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round(updatedAt.toDouble() * margin * 100) / 100

    fun snapshot(): ServicePlanEntry {
        return ServicePlanEntry(
            code = code,
            title = title,
            updatedAt = updatedAt,
            revision = revision,
            currency = currency,
            lane = lane,
            externalRef = externalRef,
            margin = margin,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service plan entry in status open. */
fun newServicePlanEntry(code: String, title: String): ServicePlanEntry {
    return ServicePlanEntry(
        code = code,
        title = title,
        updatedAt = 0,
        revision = 0,
        currency = "",
        lane = "",
        externalRef = "",
        margin = 0.0,
        phone = "",
        status = ServicePlanEntryStatus.OPEN,
    )
}
