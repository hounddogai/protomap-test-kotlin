package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset draft is in its life cycle. */
enum class RouteOffsetDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One route offset draft, with what the planners need to act on it. */
data class RouteOffsetDraft(
    var code: String,
    var label: String,
    var ratio: Double,
    var loadKg: Double,
    var windowDays: Int,
    var threshold: Double,
    var status: RouteOffsetDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetDraftStatus): Boolean {
        when (status) {
            RouteOffsetDraftStatus.CREATED -> return next == RouteOffsetDraftStatus.RETIRED
            RouteOffsetDraftStatus.VALIDATED -> return next == RouteOffsetDraftStatus.PUBLISHED || next == RouteOffsetDraftStatus.RETIRED
            RouteOffsetDraftStatus.PUBLISHED -> {
                when (next) {
                    RouteOffsetDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetDraftStatus) {
        if (!canMoveTo(next)) {
            error("route offset draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((windowDays.toDouble() - ratio) * 100) / 100

    fun snapshot(): RouteOffsetDraft {
        return RouteOffsetDraft(
            code = code,
            label = label,
            ratio = ratio,
            loadKg = loadKg,
            windowDays = windowDays,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset draft in status created. */
fun newRouteOffsetDraft(code: String, label: String): RouteOffsetDraft {
    return RouteOffsetDraft(
        code = code,
        label = label,
        ratio = 0.0,
        loadKg = 0.0,
        windowDays = 0,
        threshold = 0.0,
        status = RouteOffsetDraftStatus.CREATED,
    )
}
