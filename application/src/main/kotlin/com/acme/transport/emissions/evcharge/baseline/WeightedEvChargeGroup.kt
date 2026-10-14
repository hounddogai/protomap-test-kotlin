package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted ev charge group is in its life cycle. */
enum class WeightedEvChargeGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted ev charge group. Its code is unique within a tenant. */
data class WeightedEvChargeGroup(
    var code: String,
    var title: String,
    var active: Boolean,
    var distanceKm: Double,
    var channel: String,
    var graceDays: Int,
    var status: WeightedEvChargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeGroupStatus): Boolean {
        when (status) {
            WeightedEvChargeGroupStatus.NEW -> return next == WeightedEvChargeGroupStatus.DONE || next == WeightedEvChargeGroupStatus.FAILED
            WeightedEvChargeGroupStatus.QUEUED -> {
                when (next) {
                    WeightedEvChargeGroupStatus.RUNNING, WeightedEvChargeGroupStatus.FAILED -> return true
                }
                return false
            }
            WeightedEvChargeGroupStatus.RUNNING -> return next == WeightedEvChargeGroupStatus.FAILED
            WeightedEvChargeGroupStatus.DONE -> {
                when (next) {
                    WeightedEvChargeGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round((graceDays.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): WeightedEvChargeGroup {
        return WeightedEvChargeGroup(
            code = code,
            title = title,
            active = active,
            distanceKm = distanceKm,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted ev charge group in status new. */
fun newWeightedEvChargeGroup(code: String, title: String): WeightedEvChargeGroup {
    return WeightedEvChargeGroup(
        code = code,
        title = title,
        active = false,
        distanceKm = 0.0,
        channel = "",
        graceDays = 0,
        status = WeightedEvChargeGroupStatus.NEW,
    )
}
