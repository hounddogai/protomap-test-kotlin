package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal service plan threshold records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a seasonal service plan threshold is in its life cycle. */
enum class SeasonalServicePlanThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal service plan threshold. Its code is unique within a tenant. */
data class SeasonalServicePlanThreshold(
    var code: String,
    var label: String,
    var sequence: Int,
    var routeCode: String,
    var segment: String,
    var durationMinutes: Int,
    var region: String,
    var retries: Int,
    var ownerId: String,
    var source: String,
    var status: SeasonalServicePlanThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServicePlanThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServicePlanThresholdStatus): Boolean {
        when (status) {
            SeasonalServicePlanThresholdStatus.NEW -> {
                when (next) {
                    SeasonalServicePlanThresholdStatus.QUEUED -> return true
                }
                return false
            }
            SeasonalServicePlanThresholdStatus.QUEUED -> {
                when (next) {
                    SeasonalServicePlanThresholdStatus.RUNNING -> return true
                }
                return false
            }
            SeasonalServicePlanThresholdStatus.RUNNING -> return next == SeasonalServicePlanThresholdStatus.FAILED
            SeasonalServicePlanThresholdStatus.DONE -> return next == SeasonalServicePlanThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalServicePlanThresholdStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service plan threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((durationMinutes.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): SeasonalServicePlanThreshold {
        return SeasonalServicePlanThreshold(
            code = code,
            label = label,
            sequence = sequence,
            routeCode = routeCode,
            segment = segment,
            durationMinutes = durationMinutes,
            region = region,
            retries = retries,
            ownerId = ownerId,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal service plan threshold in status new. */
fun newSeasonalServicePlanThreshold(code: String, label: String): SeasonalServicePlanThreshold {
    return SeasonalServicePlanThreshold(
        code = code,
        label = label,
        sequence = 0,
        routeCode = "",
        segment = "",
        durationMinutes = 0,
        region = "",
        retries = 0,
        ownerId = "",
        source = "",
        status = SeasonalServicePlanThresholdStatus.NEW,
    )
}
