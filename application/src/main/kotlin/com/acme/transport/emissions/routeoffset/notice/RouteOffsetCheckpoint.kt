package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset checkpoint is in its life cycle. */
enum class RouteOffsetCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A route offset checkpoint. Its code is unique within a tenant. */
data class RouteOffsetCheckpoint(
    var code: String,
    var name: String,
    var score: Double,
    var phone: String,
    var expiresAt: Int,
    var margin: Double,
    var unit: String,
    var active: Boolean,
    var lane: String,
    var tags: MutableList<String>,
    var status: RouteOffsetCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetCheckpointStatus): Boolean {
        when (status) {
            RouteOffsetCheckpointStatus.CREATED -> {
                when (next) {
                    RouteOffsetCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            RouteOffsetCheckpointStatus.VALIDATED -> return next == RouteOffsetCheckpointStatus.RETIRED
            RouteOffsetCheckpointStatus.PUBLISHED -> return next == RouteOffsetCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RouteOffsetCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("route offset checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset checkpoint"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((score + margin) * 100) / 100

    fun snapshot(): RouteOffsetCheckpoint {
        return RouteOffsetCheckpoint(
            code = code,
            name = name,
            score = score,
            phone = phone,
            expiresAt = expiresAt,
            margin = margin,
            unit = unit,
            active = active,
            lane = lane,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a route offset checkpoint in status created. */
fun newRouteOffsetCheckpoint(code: String, name: String): RouteOffsetCheckpoint {
    return RouteOffsetCheckpoint(
        code = code,
        name = name,
        score = 0.0,
        phone = "",
        expiresAt = 0,
        margin = 0.0,
        unit = "",
        active = false,
        lane = "",
        tags = mutableListOf<String>(),
        status = RouteOffsetCheckpointStatus.CREATED,
    )
}
