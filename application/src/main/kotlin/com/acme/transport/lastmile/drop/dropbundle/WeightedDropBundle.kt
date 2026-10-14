package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.normalize

// The weighted drop bundle records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a weighted drop bundle is in its life cycle. */
enum class WeightedDropBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted drop bundle. Its code is unique within a tenant. */
data class WeightedDropBundle(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var phone: String,
    var unit: String,
    var status: WeightedDropBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDropBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDropBundleStatus): Boolean {
        when (status) {
            WeightedDropBundleStatus.IDLE -> return next == WeightedDropBundleStatus.READY
            WeightedDropBundleStatus.WARMING -> return next == WeightedDropBundleStatus.READY || next == WeightedDropBundleStatus.DRAINING
            WeightedDropBundleStatus.READY -> return next == WeightedDropBundleStatus.DRAINING || next == WeightedDropBundleStatus.STOPPED
            WeightedDropBundleStatus.DRAINING -> {
                when (next) {
                    WeightedDropBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDropBundleStatus) {
        if (!canMoveTo(next)) {
            error("weighted drop bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): WeightedDropBundle {
        return WeightedDropBundle(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            phone = phone,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted drop bundle in status idle. */
fun newWeightedDropBundle(code: String, label: String): WeightedDropBundle {
    return WeightedDropBundle(
        code = code,
        label = label,
        etaMinutes = 0,
        phone = "",
        unit = "",
        status = WeightedDropBundleStatus.IDLE,
    )
}
