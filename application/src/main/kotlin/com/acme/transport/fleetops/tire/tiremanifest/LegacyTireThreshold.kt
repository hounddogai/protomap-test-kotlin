package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize

/** Where a legacy tire threshold is in its life cycle. */
enum class LegacyTireThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy tire threshold and its current status. */
data class LegacyTireThreshold(
    var code: String,
    var label: String,
    var locale: String,
    var lane: String,
    var notes: String,
    var quantity: Int,
    var status: LegacyTireThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireThresholdStatus): Boolean {
        when (status) {
            LegacyTireThresholdStatus.PROPOSED -> {
                when (next) {
                    LegacyTireThresholdStatus.REJECTED -> return true
                }
                return false
            }
            LegacyTireThresholdStatus.APPROVED -> {
                when (next) {
                    LegacyTireThresholdStatus.REJECTED, LegacyTireThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyTireThresholdStatus.REJECTED -> return next == LegacyTireThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyTireThresholdStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LegacyTireThreshold {
        return LegacyTireThreshold(
            code = code,
            label = label,
            locale = locale,
            lane = lane,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy tire threshold in status proposed. */
fun newLegacyTireThreshold(code: String, label: String): LegacyTireThreshold {
    return LegacyTireThreshold(
        code = code,
        label = label,
        locale = "",
        lane = "",
        notes = "",
        quantity = 0,
        status = LegacyTireThresholdStatus.PROPOSED,
    )
}
