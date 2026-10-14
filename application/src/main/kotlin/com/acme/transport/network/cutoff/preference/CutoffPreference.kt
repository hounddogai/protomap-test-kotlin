package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff preference records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a cutoff preference is in its life cycle. */
enum class CutoffPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a cutoff preference and its current status. */
data class CutoffPreference(
    var code: String,
    var title: String,
    var visible: Boolean,
    var dock: String,
    var version: Int,
    var discount: Double,
    var windowDays: Int,
    var driverId: String,
    var status: CutoffPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffPreferenceStatus): Boolean {
        when (status) {
            CutoffPreferenceStatus.PLANNED -> return next == CutoffPreferenceStatus.IN_PROGRESS
            CutoffPreferenceStatus.SCHEDULED -> {
                when (next) {
                    CutoffPreferenceStatus.COMPLETED, CutoffPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            CutoffPreferenceStatus.IN_PROGRESS -> return next == CutoffPreferenceStatus.COMPLETED || next == CutoffPreferenceStatus.CANCELLED
            CutoffPreferenceStatus.COMPLETED -> {
                when (next) {
                    CutoffPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("cutoff preference $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"cutoff preference"} $code"

    fun netVolume(): Double = round((discount - windowDays.toDouble()) * 100) / 100

    fun snapshot(): CutoffPreference {
        return CutoffPreference(
            code = code,
            title = title,
            visible = visible,
            dock = dock,
            version = version,
            discount = discount,
            windowDays = windowDays,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff preference in status planned. */
fun newCutoffPreference(code: String, title: String): CutoffPreference {
    return CutoffPreference(
        code = code,
        title = title,
        visible = false,
        dock = "",
        version = 0,
        discount = 0.0,
        windowDays = 0,
        driverId = "",
        status = CutoffPreferenceStatus.PLANNED,
    )
}
