package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted rest break override records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a weighted rest break override is in its life cycle. */
enum class WeightedRestBreakOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One weighted rest break override, with what the planners need to act on it. */
data class WeightedRestBreakOverride(
    var code: String,
    var label: String,
    var attempts: Int,
    var channel: String,
    var pallets: Int,
    var dock: String,
    var region: String,
    var checksum: String,
    var rate: Double,
    var status: WeightedRestBreakOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRestBreakOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRestBreakOverrideStatus): Boolean {
        when (status) {
            WeightedRestBreakOverrideStatus.PLANNED -> {
                when (next) {
                    WeightedRestBreakOverrideStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedRestBreakOverrideStatus.SCHEDULED -> return next == WeightedRestBreakOverrideStatus.IN_PROGRESS || next == WeightedRestBreakOverrideStatus.CANCELLED
            WeightedRestBreakOverrideStatus.IN_PROGRESS -> return next == WeightedRestBreakOverrideStatus.COMPLETED || next == WeightedRestBreakOverrideStatus.CANCELLED
            WeightedRestBreakOverrideStatus.COMPLETED -> return next == WeightedRestBreakOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedRestBreakOverrideStatus) {
        if (!canMoveTo(next)) {
            error("weighted rest break override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round(pallets.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): WeightedRestBreakOverride {
        return WeightedRestBreakOverride(
            code = code,
            label = label,
            attempts = attempts,
            channel = channel,
            pallets = pallets,
            dock = dock,
            region = region,
            checksum = checksum,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted rest break override in status planned. */
fun newWeightedRestBreakOverride(code: String, label: String): WeightedRestBreakOverride {
    return WeightedRestBreakOverride(
        code = code,
        label = label,
        attempts = 0,
        channel = "",
        pallets = 0,
        dock = "",
        region = "",
        checksum = "",
        rate = 0.0,
        status = WeightedRestBreakOverrideStatus.PLANNED,
    )
}
