package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff override is in its life cycle. */
enum class CutoffOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One cutoff override, with what the planners need to act on it. */
data class CutoffOverride(
    var code: String,
    var label: String,
    var maximum: Double,
    var sequence: Int,
    var currency: String,
    var ratio: Double,
    var unit: String,
    var status: CutoffOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffOverrideStatus): Boolean {
        when (status) {
            CutoffOverrideStatus.OPEN -> return next == CutoffOverrideStatus.ARCHIVED
            CutoffOverrideStatus.IN_REVIEW -> {
                when (next) {
                    CutoffOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            CutoffOverrideStatus.RESOLVED -> return next == CutoffOverrideStatus.REOPENED
            CutoffOverrideStatus.REOPENED -> return next == CutoffOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CutoffOverrideStatus) {
        if (!canMoveTo(next)) {
            error("cutoff override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cutoff override"} $code"

    fun netLoad(): Double = round((sequence.toDouble() - ratio) * 100) / 100

    fun snapshot(): CutoffOverride {
        return CutoffOverride(
            code = code,
            label = label,
            maximum = maximum,
            sequence = sequence,
            currency = currency,
            ratio = ratio,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a cutoff override in status open. */
fun newCutoffOverride(code: String, label: String): CutoffOverride {
    return CutoffOverride(
        code = code,
        label = label,
        maximum = 0.0,
        sequence = 0,
        currency = "",
        ratio = 0.0,
        unit = "",
        status = CutoffOverrideStatus.OPEN,
    )
}
