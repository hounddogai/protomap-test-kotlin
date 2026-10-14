package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.normalize

/** Where a legacy dwell preference is in its life cycle. */
enum class LegacyDwellPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy dwell preference and its current status. */
data class LegacyDwellPreference(
    var code: String,
    var name: String,
    var batchSize: Int,
    var unit: String,
    var shiftCode: String,
    var status: LegacyDwellPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDwellPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDwellPreferenceStatus): Boolean {
        when (status) {
            LegacyDwellPreferenceStatus.PLANNED -> {
                when (next) {
                    LegacyDwellPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDwellPreferenceStatus.SCHEDULED -> return next == LegacyDwellPreferenceStatus.IN_PROGRESS
            LegacyDwellPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyDwellPreferenceStatus.COMPLETED, LegacyDwellPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDwellPreferenceStatus.COMPLETED -> return next == LegacyDwellPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyDwellPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("legacy dwell preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): LegacyDwellPreference {
        return LegacyDwellPreference(
            code = code,
            name = name,
            batchSize = batchSize,
            unit = unit,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy dwell preference in status planned. */
fun newLegacyDwellPreference(code: String, name: String): LegacyDwellPreference {
    return LegacyDwellPreference(
        code = code,
        name = name,
        batchSize = 0,
        unit = "",
        shiftCode = "",
        status = LegacyDwellPreferenceStatus.PLANNED,
    )
}
