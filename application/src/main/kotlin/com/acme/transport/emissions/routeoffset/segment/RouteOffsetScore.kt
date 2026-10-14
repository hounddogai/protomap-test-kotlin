package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset score is in its life cycle. */
enum class RouteOffsetScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One route offset score, with what the planners need to act on it. */
data class RouteOffsetScore(
    var code: String,
    var label: String,
    var depot: String,
    var notes: String,
    var updatedAt: Int,
    var dock: String,
    var sequence: Int,
    var category: String,
    var attributes: MutableMap<String, String>,
    var status: RouteOffsetScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetScoreStatus): Boolean {
        when (status) {
            RouteOffsetScoreStatus.PROPOSED -> return next == RouteOffsetScoreStatus.APPROVED || next == RouteOffsetScoreStatus.REJECTED
            RouteOffsetScoreStatus.APPROVED -> {
                when (next) {
                    RouteOffsetScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            RouteOffsetScoreStatus.REJECTED -> {
                when (next) {
                    RouteOffsetScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetScoreStatus) {
        if (!canMoveTo(next)) {
            error("route offset score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((updatedAt.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetScore {
        return RouteOffsetScore(
            code = code,
            label = label,
            depot = depot,
            notes = notes,
            updatedAt = updatedAt,
            dock = dock,
            sequence = sequence,
            category = category,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a route offset score in status proposed. */
fun newRouteOffsetScore(code: String, label: String): RouteOffsetScore {
    return RouteOffsetScore(
        code = code,
        label = label,
        depot = "",
        notes = "",
        updatedAt = 0,
        dock = "",
        sequence = 0,
        category = "",
        attributes = mutableMapOf<String, String>(),
        status = RouteOffsetScoreStatus.PROPOSED,
    )
}
