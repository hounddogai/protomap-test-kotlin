package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback route offset mapping records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a fallback route offset mapping is in its life cycle. */
enum class FallbackRouteOffsetMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback route offset mapping, with what the planners need to act on it. */
data class FallbackRouteOffsetMapping(
    var code: String,
    var label: String,
    var stopCount: Int,
    var unit: String,
    var ratio: Double,
    var status: FallbackRouteOffsetMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetMappingStatus): Boolean {
        when (status) {
            FallbackRouteOffsetMappingStatus.DRAFT -> return next == FallbackRouteOffsetMappingStatus.CLOSED
            FallbackRouteOffsetMappingStatus.PENDING -> return next == FallbackRouteOffsetMappingStatus.ACTIVE || next == FallbackRouteOffsetMappingStatus.SUSPENDED
            FallbackRouteOffsetMappingStatus.ACTIVE -> {
                when (next) {
                    FallbackRouteOffsetMappingStatus.SUSPENDED, FallbackRouteOffsetMappingStatus.CLOSED -> return true
                }
                return false
            }
            FallbackRouteOffsetMappingStatus.SUSPENDED -> return next == FallbackRouteOffsetMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetMappingStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((ratio - stopCount.toDouble()) * 100) / 100

    fun snapshot(): FallbackRouteOffsetMapping {
        return FallbackRouteOffsetMapping(
            code = code,
            label = label,
            stopCount = stopCount,
            unit = unit,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback route offset mapping in status draft. */
fun newFallbackRouteOffsetMapping(code: String, label: String): FallbackRouteOffsetMapping {
    return FallbackRouteOffsetMapping(
        code = code,
        label = label,
        stopCount = 0,
        unit = "",
        ratio = 0.0,
        status = FallbackRouteOffsetMappingStatus.DRAFT,
    )
}
