package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling near miss adjustment is in its life cycle. */
enum class RollingNearMissAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling near miss adjustment as the near miss team keeps it. */
data class RollingNearMissAdjustment(
    var code: String,
    var label: String,
    var channel: String,
    var score: Double,
    var externalRef: String,
    var dock: String,
    var vehicleId: String,
    var distanceKm: Double,
    var budget: Double,
    var phone: String,
    var status: RollingNearMissAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNearMissAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNearMissAdjustmentStatus): Boolean {
        when (status) {
            RollingNearMissAdjustmentStatus.CREATED -> {
                when (next) {
                    RollingNearMissAdjustmentStatus.VALIDATED, RollingNearMissAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
            RollingNearMissAdjustmentStatus.VALIDATED -> return next == RollingNearMissAdjustmentStatus.RETIRED
            RollingNearMissAdjustmentStatus.PUBLISHED -> return next == RollingNearMissAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingNearMissAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling near miss adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(distanceKm * score * 100) / 100

    fun snapshot(): RollingNearMissAdjustment {
        return RollingNearMissAdjustment(
            code = code,
            label = label,
            channel = channel,
            score = score,
            externalRef = externalRef,
            dock = dock,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            budget = budget,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling near miss adjustment in status created. */
fun newRollingNearMissAdjustment(code: String, label: String): RollingNearMissAdjustment {
    return RollingNearMissAdjustment(
        code = code,
        label = label,
        channel = "",
        score = 0.0,
        externalRef = "",
        dock = "",
        vehicleId = "",
        distanceKm = 0.0,
        budget = 0.0,
        phone = "",
        status = RollingNearMissAdjustmentStatus.CREATED,
    )
}
