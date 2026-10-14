package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area draft is in its life cycle. */
enum class ServiceAreaDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service area draft as the service area team keeps it. */
data class ServiceAreaDraft(
    var code: String,
    var name: String,
    var contactEmail: String,
    var reviewer: String,
    var depot: String,
    var rate: Double,
    var batchSize: Int,
    var fuelLiters: Double,
    var trailerId: String,
    var windowDays: Int,
    var routeCode: String,
    var status: ServiceAreaDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaDraftStatus): Boolean {
        when (status) {
            ServiceAreaDraftStatus.IDLE -> return next == ServiceAreaDraftStatus.READY
            ServiceAreaDraftStatus.WARMING -> return next == ServiceAreaDraftStatus.STOPPED
            ServiceAreaDraftStatus.READY -> return next == ServiceAreaDraftStatus.DRAINING || next == ServiceAreaDraftStatus.STOPPED
            ServiceAreaDraftStatus.DRAINING -> {
                when (next) {
                    ServiceAreaDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaDraftStatus) {
        if (!canMoveTo(next)) {
            error("service area draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((windowDays.toDouble() - rate) * 100) / 100

    fun snapshot(): ServiceAreaDraft {
        return ServiceAreaDraft(
            code = code,
            name = name,
            contactEmail = contactEmail,
            reviewer = reviewer,
            depot = depot,
            rate = rate,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            windowDays = windowDays,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a service area draft in status idle. */
fun newServiceAreaDraft(code: String, name: String): ServiceAreaDraft {
    return ServiceAreaDraft(
        code = code,
        name = name,
        contactEmail = "",
        reviewer = "",
        depot = "",
        rate = 0.0,
        batchSize = 0,
        fuelLiters = 0.0,
        trailerId = "",
        windowDays = 0,
        routeCode = "",
        status = ServiceAreaDraftStatus.IDLE,
    )
}
