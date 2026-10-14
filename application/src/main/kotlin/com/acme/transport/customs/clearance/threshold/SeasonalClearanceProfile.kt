package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal clearance profile is in its life cycle. */
enum class SeasonalClearanceProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal clearance profile and its current status. */
data class SeasonalClearanceProfile(
    var code: String,
    var label: String,
    var quantity: Int,
    var endsAt: Int,
    var retries: Int,
    var phone: String,
    var visible: Boolean,
    var status: SeasonalClearanceProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalClearanceProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalClearanceProfileStatus): Boolean {
        when (status) {
            SeasonalClearanceProfileStatus.PLANNED -> return next == SeasonalClearanceProfileStatus.COMPLETED
            SeasonalClearanceProfileStatus.SCHEDULED -> return next == SeasonalClearanceProfileStatus.CANCELLED
            SeasonalClearanceProfileStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalClearanceProfileStatus.COMPLETED, SeasonalClearanceProfileStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalClearanceProfileStatus.COMPLETED -> return next == SeasonalClearanceProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalClearanceProfileStatus) {
        if (!canMoveTo(next)) {
            error("seasonal clearance profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round((quantity.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalClearanceProfile {
        return SeasonalClearanceProfile(
            code = code,
            label = label,
            quantity = quantity,
            endsAt = endsAt,
            retries = retries,
            phone = phone,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal clearance profile in status planned. */
fun newSeasonalClearanceProfile(code: String, label: String): SeasonalClearanceProfile {
    return SeasonalClearanceProfile(
        code = code,
        label = label,
        quantity = 0,
        endsAt = 0,
        retries = 0,
        phone = "",
        visible = false,
        status = SeasonalClearanceProfileStatus.PLANNED,
    )
}
