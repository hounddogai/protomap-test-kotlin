package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary drop threshold is in its life cycle. */
enum class PrimaryDropThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary drop threshold as the drop team keeps it. */
data class PrimaryDropThreshold(
    var code: String,
    var name: String,
    var margin: Double,
    var currency: String,
    var endsAt: Int,
    var status: PrimaryDropThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDropThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDropThresholdStatus): Boolean {
        when (status) {
            PrimaryDropThresholdStatus.PLANNED -> return next == PrimaryDropThresholdStatus.COMPLETED || next == PrimaryDropThresholdStatus.CANCELLED
            PrimaryDropThresholdStatus.SCHEDULED -> return next == PrimaryDropThresholdStatus.IN_PROGRESS
            PrimaryDropThresholdStatus.IN_PROGRESS -> return next == PrimaryDropThresholdStatus.COMPLETED || next == PrimaryDropThresholdStatus.CANCELLED
            PrimaryDropThresholdStatus.COMPLETED -> {
                when (next) {
                    PrimaryDropThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDropThresholdStatus) {
        if (!canMoveTo(next)) {
            error("primary drop threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary drop threshold"} $code"

    fun projectedTotal(): Double = round((endsAt.toDouble() + margin) * 100) / 100

    fun snapshot(): PrimaryDropThreshold {
        return PrimaryDropThreshold(
            code = code,
            name = name,
            margin = margin,
            currency = currency,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary drop threshold in status planned. */
fun newPrimaryDropThreshold(code: String, name: String): PrimaryDropThreshold {
    return PrimaryDropThreshold(
        code = code,
        name = name,
        margin = 0.0,
        currency = "",
        endsAt = 0,
        status = PrimaryDropThresholdStatus.PLANNED,
    )
}
