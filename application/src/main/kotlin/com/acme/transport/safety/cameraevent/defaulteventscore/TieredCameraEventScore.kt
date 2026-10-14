package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered camera event score records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a tiered camera event score is in its life cycle. */
enum class TieredCameraEventScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered camera event score. Its code is unique within a tenant. */
data class TieredCameraEventScore(
    var code: String,
    var title: String,
    var channel: String,
    var minimum: Double,
    var weight: Double,
    var margin: Double,
    var floorValue: Double,
    var lane: String,
    var budget: Double,
    var carrier: String,
    var status: TieredCameraEventScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCameraEventScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCameraEventScoreStatus): Boolean {
        when (status) {
            TieredCameraEventScoreStatus.REQUESTED -> return next == TieredCameraEventScoreStatus.GRANTED || next == TieredCameraEventScoreStatus.REVOKED
            TieredCameraEventScoreStatus.GRANTED -> {
                when (next) {
                    TieredCameraEventScoreStatus.REVOKED, TieredCameraEventScoreStatus.EXPIRED -> return true
                }
                return false
            }
            TieredCameraEventScoreStatus.REVOKED -> return next == TieredCameraEventScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredCameraEventScoreStatus) {
        if (!canMoveTo(next)) {
            error("tiered camera event score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered camera event score"} $code"

    fun netExposure(): Double = round((minimum + margin) * 100) / 100

    fun snapshot(): TieredCameraEventScore {
        return TieredCameraEventScore(
            code = code,
            title = title,
            channel = channel,
            minimum = minimum,
            weight = weight,
            margin = margin,
            floorValue = floorValue,
            lane = lane,
            budget = budget,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered camera event score in status requested. */
fun newTieredCameraEventScore(code: String, title: String): TieredCameraEventScore {
    return TieredCameraEventScore(
        code = code,
        title = title,
        channel = "",
        minimum = 0.0,
        weight = 0.0,
        margin = 0.0,
        floorValue = 0.0,
        lane = "",
        budget = 0.0,
        carrier = "",
        status = TieredCameraEventScoreStatus.REQUESTED,
    )
}
