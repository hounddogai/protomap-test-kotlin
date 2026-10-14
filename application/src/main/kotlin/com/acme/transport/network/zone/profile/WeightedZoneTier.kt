package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted zone tier is in its life cycle. */
enum class WeightedZoneTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted zone tier, with what the planners need to act on it. */
data class WeightedZoneTier(
    var code: String,
    var label: String,
    var capacity: Int,
    var routeCode: String,
    var shiftCode: String,
    var stopCount: Int,
    var status: WeightedZoneTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedZoneTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedZoneTierStatus): Boolean {
        when (status) {
            WeightedZoneTierStatus.NEW -> {
                when (next) {
                    WeightedZoneTierStatus.RUNNING, WeightedZoneTierStatus.DONE -> return true
                }
                return false
            }
            WeightedZoneTierStatus.QUEUED -> return next == WeightedZoneTierStatus.RUNNING || next == WeightedZoneTierStatus.FAILED
            WeightedZoneTierStatus.RUNNING -> return next == WeightedZoneTierStatus.DONE || next == WeightedZoneTierStatus.FAILED
            WeightedZoneTierStatus.DONE -> {
                when (next) {
                    WeightedZoneTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedZoneTierStatus) {
        if (!canMoveTo(next)) {
            error("weighted zone tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted zone tier"} $code"

    fun netLoad(): Double = round((stopCount.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedZoneTier {
        return WeightedZoneTier(
            code = code,
            label = label,
            capacity = capacity,
            routeCode = routeCode,
            shiftCode = shiftCode,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted zone tier in status new. */
fun newWeightedZoneTier(code: String, label: String): WeightedZoneTier {
    return WeightedZoneTier(
        code = code,
        label = label,
        capacity = 0,
        routeCode = "",
        shiftCode = "",
        stopCount = 0,
        status = WeightedZoneTierStatus.NEW,
    )
}
