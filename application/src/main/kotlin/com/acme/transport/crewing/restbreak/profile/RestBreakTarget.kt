package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break target is in its life cycle. */
enum class RestBreakTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rest break target. Its code is unique within a tenant. */
data class RestBreakTarget(
    var code: String,
    var label: String,
    var depot: String,
    var rate: Double,
    var lane: String,
    var pinned: Boolean,
    var channel: String,
    var sequence: Int,
    var ratio: Double,
    var floorValue: Double,
    var status: RestBreakTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakTargetStatus): Boolean {
        when (status) {
            RestBreakTargetStatus.CREATED -> return next == RestBreakTargetStatus.VALIDATED
            RestBreakTargetStatus.VALIDATED -> {
                when (next) {
                    RestBreakTargetStatus.PUBLISHED -> return true
                }
                return false
            }
            RestBreakTargetStatus.PUBLISHED -> {
                when (next) {
                    RestBreakTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakTargetStatus) {
        if (!canMoveTo(next)) {
            error("rest break target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rest break target"} $code"

    fun adjustedExposure(): Double = round((floorValue + sequence.toDouble()) * 100) / 100

    fun snapshot(): RestBreakTarget {
        return RestBreakTarget(
            code = code,
            label = label,
            depot = depot,
            rate = rate,
            lane = lane,
            pinned = pinned,
            channel = channel,
            sequence = sequence,
            ratio = ratio,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rest break target in status created. */
fun newRestBreakTarget(code: String, label: String): RestBreakTarget {
    return RestBreakTarget(
        code = code,
        label = label,
        depot = "",
        rate = 0.0,
        lane = "",
        pinned = false,
        channel = "",
        sequence = 0,
        ratio = 0.0,
        floorValue = 0.0,
        status = RestBreakTargetStatus.CREATED,
    )
}
