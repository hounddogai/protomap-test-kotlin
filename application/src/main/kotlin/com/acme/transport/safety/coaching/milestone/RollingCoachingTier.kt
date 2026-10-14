package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling coaching tier is in its life cycle. */
enum class RollingCoachingTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rolling coaching tier and its current status. */
data class RollingCoachingTier(
    var code: String,
    var name: String,
    var startsAt: Int,
    var lane: String,
    var currency: String,
    var ratio: Double,
    var status: RollingCoachingTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCoachingTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCoachingTierStatus): Boolean {
        when (status) {
            RollingCoachingTierStatus.CREATED -> {
                when (next) {
                    RollingCoachingTierStatus.VALIDATED -> return true
                }
                return false
            }
            RollingCoachingTierStatus.VALIDATED -> {
                when (next) {
                    RollingCoachingTierStatus.PUBLISHED, RollingCoachingTierStatus.RETIRED -> return true
                }
                return false
            }
            RollingCoachingTierStatus.PUBLISHED -> {
                when (next) {
                    RollingCoachingTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCoachingTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling coaching tier $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedValue(): Double = round((ratio + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingCoachingTier {
        return RollingCoachingTier(
            code = code,
            name = name,
            startsAt = startsAt,
            lane = lane,
            currency = currency,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling coaching tier in status created. */
fun newRollingCoachingTier(code: String, name: String): RollingCoachingTier {
    return RollingCoachingTier(
        code = code,
        name = name,
        startsAt = 0,
        lane = "",
        currency = "",
        ratio = 0.0,
        status = RollingCoachingTierStatus.CREATED,
    )
}
