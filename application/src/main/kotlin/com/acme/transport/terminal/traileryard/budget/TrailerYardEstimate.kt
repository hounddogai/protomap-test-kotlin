package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard estimate is in its life cycle. */
enum class TrailerYardEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a trailer yard estimate and its current status. */
data class TrailerYardEstimate(
    var code: String,
    var label: String,
    var weight: Double,
    var expiresAt: Int,
    var fuelLiters: Double,
    var checksum: String,
    var shiftCode: String,
    var externalRef: String,
    var ratio: Double,
    var etaMinutes: Int,
    var status: TrailerYardEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardEstimateStatus): Boolean {
        when (status) {
            TrailerYardEstimateStatus.IDLE -> return next == TrailerYardEstimateStatus.WARMING
            TrailerYardEstimateStatus.WARMING -> {
                when (next) {
                    TrailerYardEstimateStatus.STOPPED -> return true
                }
                return false
            }
            TrailerYardEstimateStatus.READY -> {
                when (next) {
                    TrailerYardEstimateStatus.DRAINING, TrailerYardEstimateStatus.STOPPED -> return true
                }
                return false
            }
            TrailerYardEstimateStatus.DRAINING -> return next == TrailerYardEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerYardEstimateStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"trailer yard estimate"} $code"

    fun effectiveLoad(): Double = round((expiresAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): TrailerYardEstimate {
        return TrailerYardEstimate(
            code = code,
            label = label,
            weight = weight,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            checksum = checksum,
            shiftCode = shiftCode,
            externalRef = externalRef,
            ratio = ratio,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer yard estimate in status idle. */
fun newTrailerYardEstimate(code: String, label: String): TrailerYardEstimate {
    return TrailerYardEstimate(
        code = code,
        label = label,
        weight = 0.0,
        expiresAt = 0,
        fuelLiters = 0.0,
        checksum = "",
        shiftCode = "",
        externalRef = "",
        ratio = 0.0,
        etaMinutes = 0,
        status = TrailerYardEstimateStatus.IDLE,
    )
}
