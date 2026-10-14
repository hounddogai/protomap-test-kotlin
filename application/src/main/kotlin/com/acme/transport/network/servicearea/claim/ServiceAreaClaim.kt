package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area claim records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area claim is in its life cycle. */
enum class ServiceAreaClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a service area claim and its current status. */
data class ServiceAreaClaim(
    var code: String,
    var title: String,
    var locale: String,
    var amount: Double,
    var discount: Double,
    var ratio: Double,
    var externalRef: String,
    var quantity: Int,
    var timezone: String,
    var fuelLiters: Double,
    var status: ServiceAreaClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaClaimStatus): Boolean {
        when (status) {
            ServiceAreaClaimStatus.CREATED -> return next == ServiceAreaClaimStatus.PUBLISHED
            ServiceAreaClaimStatus.VALIDATED -> {
                when (next) {
                    ServiceAreaClaimStatus.PUBLISHED -> return true
                }
                return false
            }
            ServiceAreaClaimStatus.PUBLISHED -> {
                when (next) {
                    ServiceAreaClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaClaimStatus) {
        if (!canMoveTo(next)) {
            error("service area claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area claim"} $code"

    fun projectedExposure(): Double = round((amount - quantity.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaClaim {
        return ServiceAreaClaim(
            code = code,
            title = title,
            locale = locale,
            amount = amount,
            discount = discount,
            ratio = ratio,
            externalRef = externalRef,
            quantity = quantity,
            timezone = timezone,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a service area claim in status created. */
fun newServiceAreaClaim(code: String, title: String): ServiceAreaClaim {
    return ServiceAreaClaim(
        code = code,
        title = title,
        locale = "",
        amount = 0.0,
        discount = 0.0,
        ratio = 0.0,
        externalRef = "",
        quantity = 0,
        timezone = "",
        fuelLiters = 0.0,
        status = ServiceAreaClaimStatus.CREATED,
    )
}
