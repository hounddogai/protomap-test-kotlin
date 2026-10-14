package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer mapping is in its life cycle. */
enum class TrailerMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One trailer mapping, with what the planners need to act on it. */
data class TrailerMapping(
    var code: String,
    var title: String,
    var category: String,
    var version: Int,
    var cost: Double,
    var minimum: Double,
    var status: TrailerMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerMappingStatus): Boolean {
        when (status) {
            TrailerMappingStatus.IDLE -> {
                when (next) {
                    TrailerMappingStatus.WARMING -> return true
                }
                return false
            }
            TrailerMappingStatus.WARMING -> {
                when (next) {
                    TrailerMappingStatus.DRAINING -> return true
                }
                return false
            }
            TrailerMappingStatus.READY -> return next == TrailerMappingStatus.STOPPED
            TrailerMappingStatus.DRAINING -> return next == TrailerMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerMappingStatus) {
        if (!canMoveTo(next)) {
            error("trailer mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"trailer mapping"} $code"

    fun effectiveExposure(): Double = round(version.toDouble() * cost * 100) / 100

    fun snapshot(): TrailerMapping {
        return TrailerMapping(
            code = code,
            title = title,
            category = category,
            version = version,
            cost = cost,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer mapping in status idle. */
fun newTrailerMapping(code: String, title: String): TrailerMapping {
    return TrailerMapping(
        code = code,
        title = title,
        category = "",
        version = 0,
        cost = 0.0,
        minimum = 0.0,
        status = TrailerMappingStatus.IDLE,
    )
}
