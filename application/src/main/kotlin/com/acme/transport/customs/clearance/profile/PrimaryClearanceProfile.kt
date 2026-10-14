package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary clearance profile records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a primary clearance profile is in its life cycle. */
enum class PrimaryClearanceProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary clearance profile. Its code is unique within a tenant. */
data class PrimaryClearanceProfile(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var fuelLiters: Double,
    var externalRef: String,
    var rate: Double,
    var margin: Double,
    var description: String,
    var quantity: Int,
    var stopCount: Int,
    var version: Int,
    var status: PrimaryClearanceProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryClearanceProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryClearanceProfileStatus): Boolean {
        when (status) {
            PrimaryClearanceProfileStatus.REQUESTED -> {
                when (next) {
                    PrimaryClearanceProfileStatus.REVOKED, PrimaryClearanceProfileStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryClearanceProfileStatus.GRANTED -> {
                when (next) {
                    PrimaryClearanceProfileStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryClearanceProfileStatus.REVOKED -> return next == PrimaryClearanceProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryClearanceProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary clearance profile $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((rate + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryClearanceProfile {
        return PrimaryClearanceProfile(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            rate = rate,
            margin = margin,
            description = description,
            quantity = quantity,
            stopCount = stopCount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary clearance profile in status requested. */
fun newPrimaryClearanceProfile(code: String, name: String): PrimaryClearanceProfile {
    return PrimaryClearanceProfile(
        code = code,
        name = name,
        etaMinutes = 0,
        fuelLiters = 0.0,
        externalRef = "",
        rate = 0.0,
        margin = 0.0,
        description = "",
        quantity = 0,
        stopCount = 0,
        version = 0,
        status = PrimaryClearanceProfileStatus.REQUESTED,
    )
}
