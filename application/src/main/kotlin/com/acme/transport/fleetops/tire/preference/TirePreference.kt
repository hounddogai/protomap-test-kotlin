package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire preference records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire preference is in its life cycle. */
enum class TirePreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tire preference and its current status. */
data class TirePreference(
    var code: String,
    var label: String,
    var stopCount: Int,
    var visible: Boolean,
    var amount: Double,
    var tags: MutableList<String>,
    var cost: Double,
    var maximum: Double,
    var status: TirePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TirePreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TirePreferenceStatus): Boolean {
        when (status) {
            TirePreferenceStatus.PROPOSED -> return next == TirePreferenceStatus.APPROVED
            TirePreferenceStatus.APPROVED -> {
                when (next) {
                    TirePreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            TirePreferenceStatus.REJECTED -> return next == TirePreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TirePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tire preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire preference"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round(stopCount.toDouble() * amount * 100) / 100

    fun snapshot(): TirePreference {
        return TirePreference(
            code = code,
            label = label,
            stopCount = stopCount,
            visible = visible,
            amount = amount,
            tags = tags,
            cost = cost,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire preference in status proposed. */
fun newTirePreference(code: String, label: String): TirePreference {
    return TirePreference(
        code = code,
        label = label,
        stopCount = 0,
        visible = false,
        amount = 0.0,
        tags = mutableListOf<String>(),
        cost = 0.0,
        maximum = 0.0,
        status = TirePreferenceStatus.PROPOSED,
    )
}
