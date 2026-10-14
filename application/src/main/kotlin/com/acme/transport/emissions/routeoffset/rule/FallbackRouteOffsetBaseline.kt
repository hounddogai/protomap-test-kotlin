package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset baseline is in its life cycle. */
enum class FallbackRouteOffsetBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback route offset baseline. Its code is unique within a tenant. */
data class FallbackRouteOffsetBaseline(
    var code: String,
    var name: String,
    var discount: Double,
    var graceDays: Int,
    var visible: Boolean,
    var minimum: Double,
    var label: String,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var stopCount: Int,
    var status: FallbackRouteOffsetBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetBaselineStatus): Boolean {
        when (status) {
            FallbackRouteOffsetBaselineStatus.DRAFT -> {
                when (next) {
                    FallbackRouteOffsetBaselineStatus.PENDING -> return true
                }
                return false
            }
            FallbackRouteOffsetBaselineStatus.PENDING -> return next == FallbackRouteOffsetBaselineStatus.SUSPENDED || next == FallbackRouteOffsetBaselineStatus.CLOSED
            FallbackRouteOffsetBaselineStatus.ACTIVE -> return next == FallbackRouteOffsetBaselineStatus.SUSPENDED || next == FallbackRouteOffsetBaselineStatus.CLOSED
            FallbackRouteOffsetBaselineStatus.SUSPENDED -> return next == FallbackRouteOffsetBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((stopCount.toDouble() - minimum) * 100) / 100

    fun snapshot(): FallbackRouteOffsetBaseline {
        return FallbackRouteOffsetBaseline(
            code = code,
            name = name,
            discount = discount,
            graceDays = graceDays,
            visible = visible,
            minimum = minimum,
            label = label,
            distanceKm = distanceKm,
            tags = tags,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback route offset baseline in status draft. */
fun newFallbackRouteOffsetBaseline(code: String, name: String): FallbackRouteOffsetBaseline {
    return FallbackRouteOffsetBaseline(
        code = code,
        name = name,
        discount = 0.0,
        graceDays = 0,
        visible = false,
        minimum = 0.0,
        label = "",
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        stopCount = 0,
        status = FallbackRouteOffsetBaselineStatus.DRAFT,
    )
}
