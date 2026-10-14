package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan profile records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan profile is in its life cycle. */
enum class ServicePlanProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a service plan profile and its current status. */
data class ServicePlanProfile(
    var code: String,
    var title: String,
    var archived: Boolean,
    var pallets: Int,
    var etaMinutes: Int,
    var currency: String,
    var fuelLiters: Double,
    var status: ServicePlanProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanProfileStatus): Boolean {
        when (status) {
            ServicePlanProfileStatus.REQUESTED -> return next == ServicePlanProfileStatus.REVOKED || next == ServicePlanProfileStatus.EXPIRED
            ServicePlanProfileStatus.GRANTED -> return next == ServicePlanProfileStatus.EXPIRED
            ServicePlanProfileStatus.REVOKED -> return next == ServicePlanProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanProfileStatus) {
        if (!canMoveTo(next)) {
            error("service plan profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round((fuelLiters + pallets.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanProfile {
        return ServicePlanProfile(
            code = code,
            title = title,
            archived = archived,
            pallets = pallets,
            etaMinutes = etaMinutes,
            currency = currency,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service plan profile in status requested. */
fun newServicePlanProfile(code: String, title: String): ServicePlanProfile {
    return ServicePlanProfile(
        code = code,
        title = title,
        archived = false,
        pallets = 0,
        etaMinutes = 0,
        currency = "",
        fuelLiters = 0.0,
        status = ServicePlanProfileStatus.REQUESTED,
    )
}
