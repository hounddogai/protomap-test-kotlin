package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive scorecard checkpoint is in its life cycle. */
enum class AdaptiveScorecardCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive scorecard checkpoint, with what the planners need to act on it. */
data class AdaptiveScorecardCheckpoint(
    var code: String,
    var title: String,
    var ratio: Double,
    var phone: String,
    var startsAt: Int,
    var category: String,
    var rate: Double,
    var ceiling: Double,
    var country: String,
    var active: Boolean,
    var odometer: Int,
    var status: AdaptiveScorecardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveScorecardCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveScorecardCheckpointStatus): Boolean {
        when (status) {
            AdaptiveScorecardCheckpointStatus.REQUESTED -> {
                when (next) {
                    AdaptiveScorecardCheckpointStatus.REVOKED, AdaptiveScorecardCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveScorecardCheckpointStatus.GRANTED -> {
                when (next) {
                    AdaptiveScorecardCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveScorecardCheckpointStatus.REVOKED -> {
                when (next) {
                    AdaptiveScorecardCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveScorecardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("adaptive scorecard checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive scorecard checkpoint"} $code"

    fun adjustedExposure(): Double = round(ratio * rate * 100) / 100

    fun snapshot(): AdaptiveScorecardCheckpoint {
        return AdaptiveScorecardCheckpoint(
            code = code,
            title = title,
            ratio = ratio,
            phone = phone,
            startsAt = startsAt,
            category = category,
            rate = rate,
            ceiling = ceiling,
            country = country,
            active = active,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive scorecard checkpoint in status requested. */
fun newAdaptiveScorecardCheckpoint(code: String, title: String): AdaptiveScorecardCheckpoint {
    return AdaptiveScorecardCheckpoint(
        code = code,
        title = title,
        ratio = 0.0,
        phone = "",
        startsAt = 0,
        category = "",
        rate = 0.0,
        ceiling = 0.0,
        country = "",
        active = false,
        odometer = 0,
        status = AdaptiveScorecardCheckpointStatus.REQUESTED,
    )
}
