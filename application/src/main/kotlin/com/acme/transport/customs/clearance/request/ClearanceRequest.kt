package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance request records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance request is in its life cycle. */
enum class ClearanceRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One clearance request, with what the planners need to act on it. */
data class ClearanceRequest(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var attempts: Int,
    var threshold: Double,
    var batchSize: Int,
    var ceiling: Double,
    var summary: String,
    var status: ClearanceRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceRequestStatus): Boolean {
        when (status) {
            ClearanceRequestStatus.IDLE -> {
                when (next) {
                    ClearanceRequestStatus.WARMING, ClearanceRequestStatus.DRAINING -> return true
                }
                return false
            }
            ClearanceRequestStatus.WARMING -> {
                when (next) {
                    ClearanceRequestStatus.READY, ClearanceRequestStatus.DRAINING -> return true
                }
                return false
            }
            ClearanceRequestStatus.READY -> {
                when (next) {
                    ClearanceRequestStatus.DRAINING, ClearanceRequestStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceRequestStatus.DRAINING -> return next == ClearanceRequestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ClearanceRequestStatus) {
        if (!canMoveTo(next)) {
            error("clearance request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((ceiling + threshold) * 100) / 100

    fun snapshot(): ClearanceRequest {
        return ClearanceRequest(
            code = code,
            name = name,
            distanceKm = distanceKm,
            attempts = attempts,
            threshold = threshold,
            batchSize = batchSize,
            ceiling = ceiling,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance request in status idle. */
fun newClearanceRequest(code: String, name: String): ClearanceRequest {
    return ClearanceRequest(
        code = code,
        name = name,
        distanceKm = 0.0,
        attempts = 0,
        threshold = 0.0,
        batchSize = 0,
        ceiling = 0.0,
        summary = "",
        status = ClearanceRequestStatus.IDLE,
    )
}
