package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss score is in its life cycle. */
enum class NearMissScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A near miss score. Its code is unique within a tenant. */
data class NearMissScore(
    var code: String,
    var name: String,
    var threshold: Double,
    var segment: String,
    var carrier: String,
    var discount: Double,
    var timezone: String,
    var fuelLiters: Double,
    var channel: String,
    var trailerId: String,
    var etaMinutes: Int,
    var status: NearMissScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissScoreStatus): Boolean {
        when (status) {
            NearMissScoreStatus.IDLE -> {
                when (next) {
                    NearMissScoreStatus.READY -> return true
                }
                return false
            }
            NearMissScoreStatus.WARMING -> return next == NearMissScoreStatus.READY || next == NearMissScoreStatus.STOPPED
            NearMissScoreStatus.READY -> return next == NearMissScoreStatus.STOPPED
            NearMissScoreStatus.DRAINING -> {
                when (next) {
                    NearMissScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissScoreStatus) {
        if (!canMoveTo(next)) {
            error("near miss score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss score"} $code"

    fun effectiveLoad(): Double = round(fuelLiters * discount * 100) / 100

    fun snapshot(): NearMissScore {
        return NearMissScore(
            code = code,
            name = name,
            threshold = threshold,
            segment = segment,
            carrier = carrier,
            discount = discount,
            timezone = timezone,
            fuelLiters = fuelLiters,
            channel = channel,
            trailerId = trailerId,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a near miss score in status idle. */
fun newNearMissScore(code: String, name: String): NearMissScore {
    return NearMissScore(
        code = code,
        name = name,
        threshold = 0.0,
        segment = "",
        carrier = "",
        discount = 0.0,
        timezone = "",
        fuelLiters = 0.0,
        channel = "",
        trailerId = "",
        etaMinutes = 0,
        status = NearMissScoreStatus.IDLE,
    )
}
