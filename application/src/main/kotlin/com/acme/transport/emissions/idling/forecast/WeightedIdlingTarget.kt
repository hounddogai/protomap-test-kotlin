package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted idling target is in its life cycle. */
enum class WeightedIdlingTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted idling target as the idling team keeps it. */
data class WeightedIdlingTarget(
    var code: String,
    var title: String,
    var attempts: Int,
    var checksum: String,
    var lane: String,
    var phone: String,
    var pallets: Int,
    var limit: Int,
    var routeCode: String,
    var status: WeightedIdlingTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedIdlingTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedIdlingTargetStatus): Boolean {
        when (status) {
            WeightedIdlingTargetStatus.CREATED -> {
                when (next) {
                    WeightedIdlingTargetStatus.VALIDATED, WeightedIdlingTargetStatus.RETIRED -> return true
                }
                return false
            }
            WeightedIdlingTargetStatus.VALIDATED -> {
                when (next) {
                    WeightedIdlingTargetStatus.PUBLISHED, WeightedIdlingTargetStatus.RETIRED -> return true
                }
                return false
            }
            WeightedIdlingTargetStatus.PUBLISHED -> {
                when (next) {
                    WeightedIdlingTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedIdlingTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted idling target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((limit.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): WeightedIdlingTarget {
        return WeightedIdlingTarget(
            code = code,
            title = title,
            attempts = attempts,
            checksum = checksum,
            lane = lane,
            phone = phone,
            pallets = pallets,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted idling target in status created. */
fun newWeightedIdlingTarget(code: String, title: String): WeightedIdlingTarget {
    return WeightedIdlingTarget(
        code = code,
        title = title,
        attempts = 0,
        checksum = "",
        lane = "",
        phone = "",
        pallets = 0,
        limit = 0,
        routeCode = "",
        status = WeightedIdlingTargetStatus.CREATED,
    )
}
