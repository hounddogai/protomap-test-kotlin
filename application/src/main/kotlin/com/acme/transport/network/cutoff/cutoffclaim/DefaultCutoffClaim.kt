package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default cutoff claim is in its life cycle. */
enum class DefaultCutoffClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default cutoff claim and its current status. */
data class DefaultCutoffClaim(
    var code: String,
    var name: String,
    var margin: Double,
    var distanceKm: Double,
    var lane: String,
    var odometer: Int,
    var status: DefaultCutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffClaimStatus): Boolean {
        when (status) {
            DefaultCutoffClaimStatus.NEW -> return next == DefaultCutoffClaimStatus.RUNNING || next == DefaultCutoffClaimStatus.DONE
            DefaultCutoffClaimStatus.QUEUED -> {
                when (next) {
                    DefaultCutoffClaimStatus.FAILED -> return true
                }
                return false
            }
            DefaultCutoffClaimStatus.RUNNING -> return next == DefaultCutoffClaimStatus.DONE || next == DefaultCutoffClaimStatus.FAILED
            DefaultCutoffClaimStatus.DONE -> return next == DefaultCutoffClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultCutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((distanceKm + margin) * 100) / 100

    fun snapshot(): DefaultCutoffClaim {
        return DefaultCutoffClaim(
            code = code,
            name = name,
            margin = margin,
            distanceKm = distanceKm,
            lane = lane,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default cutoff claim in status new. */
fun newDefaultCutoffClaim(code: String, name: String): DefaultCutoffClaim {
    return DefaultCutoffClaim(
        code = code,
        name = name,
        margin = 0.0,
        distanceKm = 0.0,
        lane = "",
        odometer = 0,
        status = DefaultCutoffClaimStatus.NEW,
    )
}
