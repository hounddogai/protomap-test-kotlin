package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul score is in its life cycle. */
enum class BackhaulScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A backhaul score. Its code is unique within a tenant. */
data class BackhaulScore(
    var code: String,
    var label: String,
    var odometer: Int,
    var floorValue: Double,
    var channel: String,
    var tags: MutableList<String>,
    var status: BackhaulScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulScoreStatus): Boolean {
        when (status) {
            BackhaulScoreStatus.PLANNED -> {
                when (next) {
                    BackhaulScoreStatus.CANCELLED -> return true
                }
                return false
            }
            BackhaulScoreStatus.SCHEDULED -> {
                when (next) {
                    BackhaulScoreStatus.COMPLETED, BackhaulScoreStatus.CANCELLED -> return true
                }
                return false
            }
            BackhaulScoreStatus.IN_PROGRESS -> {
                when (next) {
                    BackhaulScoreStatus.CANCELLED -> return true
                }
                return false
            }
            BackhaulScoreStatus.COMPLETED -> return next == BackhaulScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BackhaulScoreStatus) {
        if (!canMoveTo(next)) {
            error("backhaul score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((odometer.toDouble() - floorValue) * 100) / 100

    fun snapshot(): BackhaulScore {
        return BackhaulScore(
            code = code,
            label = label,
            odometer = odometer,
            floorValue = floorValue,
            channel = channel,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a backhaul score in status planned. */
fun newBackhaulScore(code: String, label: String): BackhaulScore {
    return BackhaulScore(
        code = code,
        label = label,
        odometer = 0,
        floorValue = 0.0,
        channel = "",
        tags = mutableListOf<String>(),
        status = BackhaulScoreStatus.PLANNED,
    )
}
