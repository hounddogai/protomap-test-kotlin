package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset baseline is in its life cycle. */
enum class RouteOffsetBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One route offset baseline, with what the planners need to act on it. */
data class RouteOffsetBaseline(
    var code: String,
    var title: String,
    var category: String,
    var graceDays: Int,
    var sequence: Int,
    var revision: Int,
    var locale: String,
    var margin: Double,
    var threshold: Double,
    var source: String,
    var status: RouteOffsetBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetBaselineStatus): Boolean {
        when (status) {
            RouteOffsetBaselineStatus.NEW -> return next == RouteOffsetBaselineStatus.DONE || next == RouteOffsetBaselineStatus.FAILED
            RouteOffsetBaselineStatus.QUEUED -> {
                when (next) {
                    RouteOffsetBaselineStatus.DONE, RouteOffsetBaselineStatus.FAILED -> return true
                }
                return false
            }
            RouteOffsetBaselineStatus.RUNNING -> {
                when (next) {
                    RouteOffsetBaselineStatus.FAILED -> return true
                }
                return false
            }
            RouteOffsetBaselineStatus.DONE -> {
                when (next) {
                    RouteOffsetBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetBaselineStatus) {
        if (!canMoveTo(next)) {
            error("route offset baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"route offset baseline"} $code"

    fun projectedLoad(): Double = round((sequence.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetBaseline {
        return RouteOffsetBaseline(
            code = code,
            title = title,
            category = category,
            graceDays = graceDays,
            sequence = sequence,
            revision = revision,
            locale = locale,
            margin = margin,
            threshold = threshold,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a route offset baseline in status new. */
fun newRouteOffsetBaseline(code: String, title: String): RouteOffsetBaseline {
    return RouteOffsetBaseline(
        code = code,
        title = title,
        category = "",
        graceDays = 0,
        sequence = 0,
        revision = 0,
        locale = "",
        margin = 0.0,
        threshold = 0.0,
        source = "",
        status = RouteOffsetBaselineStatus.NEW,
    )
}
