package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break digest is in its life cycle. */
enum class RestBreakDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rest break digest and its current status. */
data class RestBreakDigest(
    var code: String,
    var label: String,
    var revision: Int,
    var unit: String,
    var createdAt: Int,
    var floorValue: Double,
    var ownerId: String,
    var startsAt: Int,
    var driverId: String,
    var margin: Double,
    var durationMinutes: Int,
    var status: RestBreakDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakDigestStatus): Boolean {
        when (status) {
            RestBreakDigestStatus.PLANNED -> {
                when (next) {
                    RestBreakDigestStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakDigestStatus.SCHEDULED -> return next == RestBreakDigestStatus.IN_PROGRESS || next == RestBreakDigestStatus.COMPLETED
            RestBreakDigestStatus.IN_PROGRESS -> return next == RestBreakDigestStatus.COMPLETED
            RestBreakDigestStatus.COMPLETED -> return next == RestBreakDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RestBreakDigestStatus) {
        if (!canMoveTo(next)) {
            error("rest break digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round(startsAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): RestBreakDigest {
        return RestBreakDigest(
            code = code,
            label = label,
            revision = revision,
            unit = unit,
            createdAt = createdAt,
            floorValue = floorValue,
            ownerId = ownerId,
            startsAt = startsAt,
            driverId = driverId,
            margin = margin,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rest break digest in status planned. */
fun newRestBreakDigest(code: String, label: String): RestBreakDigest {
    return RestBreakDigest(
        code = code,
        label = label,
        revision = 0,
        unit = "",
        createdAt = 0,
        floorValue = 0.0,
        ownerId = "",
        startsAt = 0,
        driverId = "",
        margin = 0.0,
        durationMinutes = 0,
        status = RestBreakDigestStatus.PLANNED,
    )
}
