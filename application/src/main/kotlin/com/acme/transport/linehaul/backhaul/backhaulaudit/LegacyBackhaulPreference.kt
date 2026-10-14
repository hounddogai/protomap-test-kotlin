package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy backhaul preference is in its life cycle. */
enum class LegacyBackhaulPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy backhaul preference and its current status. */
data class LegacyBackhaulPreference(
    var code: String,
    var label: String,
    var ownerId: String,
    var floorValue: Double,
    var segment: String,
    var priority: Int,
    var reviewer: String,
    var name: String,
    var status: LegacyBackhaulPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulPreferenceStatus): Boolean {
        when (status) {
            LegacyBackhaulPreferenceStatus.REQUESTED -> {
                when (next) {
                    LegacyBackhaulPreferenceStatus.GRANTED, LegacyBackhaulPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            LegacyBackhaulPreferenceStatus.GRANTED -> {
                when (next) {
                    LegacyBackhaulPreferenceStatus.REVOKED, LegacyBackhaulPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyBackhaulPreferenceStatus.REVOKED -> return next == LegacyBackhaulPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((priority.toDouble() + floorValue) * 100) / 100

    fun snapshot(): LegacyBackhaulPreference {
        return LegacyBackhaulPreference(
            code = code,
            label = label,
            ownerId = ownerId,
            floorValue = floorValue,
            segment = segment,
            priority = priority,
            reviewer = reviewer,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy backhaul preference in status requested. */
fun newLegacyBackhaulPreference(code: String, label: String): LegacyBackhaulPreference {
    return LegacyBackhaulPreference(
        code = code,
        label = label,
        ownerId = "",
        floorValue = 0.0,
        segment = "",
        priority = 0,
        reviewer = "",
        name = "",
        status = LegacyBackhaulPreferenceStatus.REQUESTED,
    )
}
