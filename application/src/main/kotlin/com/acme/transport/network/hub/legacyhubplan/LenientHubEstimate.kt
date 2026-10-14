package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient hub estimate is in its life cycle. */
enum class LenientHubEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient hub estimate and its current status. */
data class LenientHubEstimate(
    var code: String,
    var name: String,
    var cost: Double,
    var externalRef: String,
    var pallets: Int,
    var stopCount: Int,
    var carrier: String,
    var status: LenientHubEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHubEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHubEstimateStatus): Boolean {
        when (status) {
            LenientHubEstimateStatus.IDLE -> {
                when (next) {
                    LenientHubEstimateStatus.READY -> return true
                }
                return false
            }
            LenientHubEstimateStatus.WARMING -> return next == LenientHubEstimateStatus.READY || next == LenientHubEstimateStatus.STOPPED
            LenientHubEstimateStatus.READY -> return next == LenientHubEstimateStatus.DRAINING || next == LenientHubEstimateStatus.STOPPED
            LenientHubEstimateStatus.DRAINING -> {
                when (next) {
                    LenientHubEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientHubEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient hub estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((stopCount.toDouble() - cost) * 100) / 100

    fun snapshot(): LenientHubEstimate {
        return LenientHubEstimate(
            code = code,
            name = name,
            cost = cost,
            externalRef = externalRef,
            pallets = pallets,
            stopCount = stopCount,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient hub estimate in status idle. */
fun newLenientHubEstimate(code: String, name: String): LenientHubEstimate {
    return LenientHubEstimate(
        code = code,
        name = name,
        cost = 0.0,
        externalRef = "",
        pallets = 0,
        stopCount = 0,
        carrier = "",
        status = LenientHubEstimateStatus.IDLE,
    )
}
