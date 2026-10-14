package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break override is in its life cycle. */
enum class RestBreakOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rest break override, with what the planners need to act on it. */
data class RestBreakOverride(
    var code: String,
    var name: String,
    var region: String,
    var startsAt: Int,
    var carrier: String,
    var revision: Int,
    var weight: Double,
    var score: Double,
    var externalRef: String,
    var driverId: String,
    var status: RestBreakOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakOverrideStatus): Boolean {
        when (status) {
            RestBreakOverrideStatus.DRAFT -> {
                when (next) {
                    RestBreakOverrideStatus.PENDING, RestBreakOverrideStatus.CLOSED -> return true
                }
                return false
            }
            RestBreakOverrideStatus.PENDING -> {
                when (next) {
                    RestBreakOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            RestBreakOverrideStatus.ACTIVE -> return next == RestBreakOverrideStatus.CLOSED
            RestBreakOverrideStatus.SUSPENDED -> return next == RestBreakOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RestBreakOverrideStatus) {
        if (!canMoveTo(next)) {
            error("rest break override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((revision.toDouble() + weight) * 100) / 100

    fun snapshot(): RestBreakOverride {
        return RestBreakOverride(
            code = code,
            name = name,
            region = region,
            startsAt = startsAt,
            carrier = carrier,
            revision = revision,
            weight = weight,
            score = score,
            externalRef = externalRef,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rest break override in status draft. */
fun newRestBreakOverride(code: String, name: String): RestBreakOverride {
    return RestBreakOverride(
        code = code,
        name = name,
        region = "",
        startsAt = 0,
        carrier = "",
        revision = 0,
        weight = 0.0,
        score = 0.0,
        externalRef = "",
        driverId = "",
        status = RestBreakOverrideStatus.DRAFT,
    )
}
