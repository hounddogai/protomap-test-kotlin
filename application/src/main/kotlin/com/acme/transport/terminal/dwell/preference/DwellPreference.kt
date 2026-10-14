package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell preference records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell preference is in its life cycle. */
enum class DwellPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell preference, with what the planners need to act on it. */
data class DwellPreference(
    var code: String,
    var label: String,
    var trailerId: String,
    var timezone: String,
    var budget: Double,
    var checksum: String,
    var shiftCode: String,
    var segment: String,
    var pallets: Int,
    var visible: Boolean,
    var carrier: String,
    var status: DwellPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellPreferenceStatus): Boolean {
        when (status) {
            DwellPreferenceStatus.DRAFT -> {
                when (next) {
                    DwellPreferenceStatus.PENDING, DwellPreferenceStatus.CLOSED -> return true
                }
                return false
            }
            DwellPreferenceStatus.PENDING -> return next == DwellPreferenceStatus.ACTIVE || next == DwellPreferenceStatus.CLOSED
            DwellPreferenceStatus.ACTIVE -> return next == DwellPreferenceStatus.CLOSED
            DwellPreferenceStatus.SUSPENDED -> return next == DwellPreferenceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DwellPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("dwell preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round(pallets.toDouble() * budget * 100) / 100

    fun snapshot(): DwellPreference {
        return DwellPreference(
            code = code,
            label = label,
            trailerId = trailerId,
            timezone = timezone,
            budget = budget,
            checksum = checksum,
            shiftCode = shiftCode,
            segment = segment,
            pallets = pallets,
            visible = visible,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a dwell preference in status draft. */
fun newDwellPreference(code: String, label: String): DwellPreference {
    return DwellPreference(
        code = code,
        label = label,
        trailerId = "",
        timezone = "",
        budget = 0.0,
        checksum = "",
        shiftCode = "",
        segment = "",
        pallets = 0,
        visible = false,
        carrier = "",
        status = DwellPreferenceStatus.DRAFT,
    )
}
