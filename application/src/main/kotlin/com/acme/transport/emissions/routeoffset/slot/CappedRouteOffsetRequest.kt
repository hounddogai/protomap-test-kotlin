package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped route offset request is in its life cycle. */
enum class CappedRouteOffsetRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capped route offset request and its current status. */
data class CappedRouteOffsetRequest(
    var code: String,
    var name: String,
    var ratio: Double,
    var floorValue: Double,
    var sequence: Int,
    var tags: MutableList<String>,
    var source: String,
    var lane: String,
    var category: String,
    var pallets: Int,
    var score: Double,
    var status: CappedRouteOffsetRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRouteOffsetRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRouteOffsetRequestStatus): Boolean {
        when (status) {
            CappedRouteOffsetRequestStatus.OPEN -> return next == CappedRouteOffsetRequestStatus.IN_REVIEW
            CappedRouteOffsetRequestStatus.IN_REVIEW -> return next == CappedRouteOffsetRequestStatus.ARCHIVED
            CappedRouteOffsetRequestStatus.RESOLVED -> {
                when (next) {
                    CappedRouteOffsetRequestStatus.REOPENED -> return true
                }
                return false
            }
            CappedRouteOffsetRequestStatus.REOPENED -> return next == CappedRouteOffsetRequestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedRouteOffsetRequestStatus) {
        if (!canMoveTo(next)) {
            error("capped route offset request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped route offset request"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((sequence.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CappedRouteOffsetRequest {
        return CappedRouteOffsetRequest(
            code = code,
            name = name,
            ratio = ratio,
            floorValue = floorValue,
            sequence = sequence,
            tags = tags,
            source = source,
            lane = lane,
            category = category,
            pallets = pallets,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped route offset request in status open. */
fun newCappedRouteOffsetRequest(code: String, name: String): CappedRouteOffsetRequest {
    return CappedRouteOffsetRequest(
        code = code,
        name = name,
        ratio = 0.0,
        floorValue = 0.0,
        sequence = 0,
        tags = mutableListOf<String>(),
        source = "",
        lane = "",
        category = "",
        pallets = 0,
        score = 0.0,
        status = CappedRouteOffsetRequestStatus.OPEN,
    )
}
