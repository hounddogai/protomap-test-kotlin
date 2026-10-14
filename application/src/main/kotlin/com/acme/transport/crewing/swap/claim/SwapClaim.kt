package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap claim records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap claim is in its life cycle. */
enum class SwapClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A swap claim. Its code is unique within a tenant. */
data class SwapClaim(
    var code: String,
    var title: String,
    var segment: String,
    var locale: String,
    var minimum: Double,
    var quantity: Int,
    var fuelLiters: Double,
    var distanceKm: Double,
    var status: SwapClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapClaimStatus): Boolean {
        when (status) {
            SwapClaimStatus.NEW -> return next == SwapClaimStatus.QUEUED || next == SwapClaimStatus.RUNNING
            SwapClaimStatus.QUEUED -> return next == SwapClaimStatus.RUNNING || next == SwapClaimStatus.DONE
            SwapClaimStatus.RUNNING -> {
                when (next) {
                    SwapClaimStatus.FAILED -> return true
                }
                return false
            }
            SwapClaimStatus.DONE -> {
                when (next) {
                    SwapClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapClaimStatus) {
        if (!canMoveTo(next)) {
            error("swap claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round(distanceKm * minimum * 100) / 100

    fun snapshot(): SwapClaim {
        return SwapClaim(
            code = code,
            title = title,
            segment = segment,
            locale = locale,
            minimum = minimum,
            quantity = quantity,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a swap claim in status new. */
fun newSwapClaim(code: String, title: String): SwapClaim {
    return SwapClaim(
        code = code,
        title = title,
        segment = "",
        locale = "",
        minimum = 0.0,
        quantity = 0,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        status = SwapClaimStatus.NEW,
    )
}
