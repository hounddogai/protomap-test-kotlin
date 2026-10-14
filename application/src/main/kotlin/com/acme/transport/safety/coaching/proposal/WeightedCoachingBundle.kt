package com.acme.transport.safety.coaching.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted coaching bundle is in its life cycle. */
enum class WeightedCoachingBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One weighted coaching bundle, with what the planners need to act on it. */
data class WeightedCoachingBundle(
    var code: String,
    var title: String,
    var revision: Int,
    var ceiling: Double,
    var windowDays: Int,
    var minimum: Double,
    var maximum: Double,
    var graceDays: Int,
    var retries: Int,
    var vehicleId: String,
    var status: WeightedCoachingBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCoachingBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCoachingBundleStatus): Boolean {
        when (status) {
            WeightedCoachingBundleStatus.REQUESTED -> {
                when (next) {
                    WeightedCoachingBundleStatus.GRANTED -> return true
                }
                return false
            }
            WeightedCoachingBundleStatus.GRANTED -> {
                when (next) {
                    WeightedCoachingBundleStatus.REVOKED -> return true
                }
                return false
            }
            WeightedCoachingBundleStatus.REVOKED -> {
                when (next) {
                    WeightedCoachingBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCoachingBundleStatus) {
        if (!canMoveTo(next)) {
            error("weighted coaching bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((maximum + retries.toDouble()) * 100) / 100

    fun snapshot(): WeightedCoachingBundle {
        return WeightedCoachingBundle(
            code = code,
            title = title,
            revision = revision,
            ceiling = ceiling,
            windowDays = windowDays,
            minimum = minimum,
            maximum = maximum,
            graceDays = graceDays,
            retries = retries,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted coaching bundle in status requested. */
fun newWeightedCoachingBundle(code: String, title: String): WeightedCoachingBundle {
    return WeightedCoachingBundle(
        code = code,
        title = title,
        revision = 0,
        ceiling = 0.0,
        windowDays = 0,
        minimum = 0.0,
        maximum = 0.0,
        graceDays = 0,
        retries = 0,
        vehicleId = "",
        status = WeightedCoachingBundleStatus.REQUESTED,
    )
}
