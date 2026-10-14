package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy tender preference records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a legacy tender preference is in its life cycle. */
enum class LegacyTenderPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy tender preference as the tender team keeps it. */
data class LegacyTenderPreference(
    var code: String,
    var label: String,
    var visible: Boolean,
    var category: String,
    var windowDays: Int,
    var unit: String,
    var weight: Double,
    var status: LegacyTenderPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTenderPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTenderPreferenceStatus): Boolean {
        when (status) {
            LegacyTenderPreferenceStatus.NEW -> {
                when (next) {
                    LegacyTenderPreferenceStatus.QUEUED, LegacyTenderPreferenceStatus.FAILED -> return true
                }
                return false
            }
            LegacyTenderPreferenceStatus.QUEUED -> return next == LegacyTenderPreferenceStatus.RUNNING || next == LegacyTenderPreferenceStatus.DONE
            LegacyTenderPreferenceStatus.RUNNING -> return next == LegacyTenderPreferenceStatus.DONE || next == LegacyTenderPreferenceStatus.FAILED
            LegacyTenderPreferenceStatus.DONE -> {
                when (next) {
                    LegacyTenderPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTenderPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("legacy tender preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((windowDays.toDouble() + weight) * 100) / 100

    fun snapshot(): LegacyTenderPreference {
        return LegacyTenderPreference(
            code = code,
            label = label,
            visible = visible,
            category = category,
            windowDays = windowDays,
            unit = unit,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy tender preference in status new. */
fun newLegacyTenderPreference(code: String, label: String): LegacyTenderPreference {
    return LegacyTenderPreference(
        code = code,
        label = label,
        visible = false,
        category = "",
        windowDays = 0,
        unit = "",
        weight = 0.0,
        status = LegacyTenderPreferenceStatus.NEW,
    )
}
