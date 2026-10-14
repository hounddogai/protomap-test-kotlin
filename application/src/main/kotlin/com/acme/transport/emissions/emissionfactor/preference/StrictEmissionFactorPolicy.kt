package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict emission factor policy is in its life cycle. */
enum class StrictEmissionFactorPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a strict emission factor policy and its current status. */
data class StrictEmissionFactorPolicy(
    var code: String,
    var name: String,
    var externalRef: String,
    var trailerId: String,
    var routeCode: String,
    var floorValue: Double,
    var updatedAt: Int,
    var depot: String,
    var pallets: Int,
    var batchSize: Int,
    var status: StrictEmissionFactorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEmissionFactorPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEmissionFactorPolicyStatus): Boolean {
        when (status) {
            StrictEmissionFactorPolicyStatus.CREATED -> {
                when (next) {
                    StrictEmissionFactorPolicyStatus.VALIDATED -> return true
                }
                return false
            }
            StrictEmissionFactorPolicyStatus.VALIDATED -> {
                when (next) {
                    StrictEmissionFactorPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictEmissionFactorPolicyStatus.PUBLISHED -> return next == StrictEmissionFactorPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictEmissionFactorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict emission factor policy $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict emission factor policy"} $code"

    fun effectiveTotal(): Double = round((batchSize.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): StrictEmissionFactorPolicy {
        return StrictEmissionFactorPolicy(
            code = code,
            name = name,
            externalRef = externalRef,
            trailerId = trailerId,
            routeCode = routeCode,
            floorValue = floorValue,
            updatedAt = updatedAt,
            depot = depot,
            pallets = pallets,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict emission factor policy in status created. */
fun newStrictEmissionFactorPolicy(code: String, name: String): StrictEmissionFactorPolicy {
    return StrictEmissionFactorPolicy(
        code = code,
        name = name,
        externalRef = "",
        trailerId = "",
        routeCode = "",
        floorValue = 0.0,
        updatedAt = 0,
        depot = "",
        pallets = 0,
        batchSize = 0,
        status = StrictEmissionFactorPolicyStatus.CREATED,
    )
}
