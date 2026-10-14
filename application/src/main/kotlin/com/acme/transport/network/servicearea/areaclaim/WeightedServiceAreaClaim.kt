package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted service area claim records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a weighted service area claim is in its life cycle. */
enum class WeightedServiceAreaClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted service area claim. Its code is unique within a tenant. */
data class WeightedServiceAreaClaim(
    var code: String,
    var name: String,
    var contactEmail: String,
    var score: Double,
    var sequence: Int,
    var externalRef: String,
    var weight: Double,
    var batchSize: Int,
    var shiftCode: String,
    var status: WeightedServiceAreaClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServiceAreaClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServiceAreaClaimStatus): Boolean {
        when (status) {
            WeightedServiceAreaClaimStatus.IDLE -> {
                when (next) {
                    WeightedServiceAreaClaimStatus.STOPPED -> return true
                }
                return false
            }
            WeightedServiceAreaClaimStatus.WARMING -> return next == WeightedServiceAreaClaimStatus.READY || next == WeightedServiceAreaClaimStatus.DRAINING
            WeightedServiceAreaClaimStatus.READY -> {
                when (next) {
                    WeightedServiceAreaClaimStatus.STOPPED -> return true
                }
                return false
            }
            WeightedServiceAreaClaimStatus.DRAINING -> {
                when (next) {
                    WeightedServiceAreaClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedServiceAreaClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted service area claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round(weight * batchSize.toDouble() * 100) / 100

    fun snapshot(): WeightedServiceAreaClaim {
        return WeightedServiceAreaClaim(
            code = code,
            name = name,
            contactEmail = contactEmail,
            score = score,
            sequence = sequence,
            externalRef = externalRef,
            weight = weight,
            batchSize = batchSize,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted service area claim in status idle. */
fun newWeightedServiceAreaClaim(code: String, name: String): WeightedServiceAreaClaim {
    return WeightedServiceAreaClaim(
        code = code,
        name = name,
        contactEmail = "",
        score = 0.0,
        sequence = 0,
        externalRef = "",
        weight = 0.0,
        batchSize = 0,
        shiftCode = "",
        status = WeightedServiceAreaClaimStatus.IDLE,
    )
}
