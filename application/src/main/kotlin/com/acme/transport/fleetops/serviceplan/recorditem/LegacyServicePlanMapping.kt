package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy service plan mapping is in its life cycle. */
enum class LegacyServicePlanMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy service plan mapping and its current status. */
data class LegacyServicePlanMapping(
    var code: String,
    var title: String,
    var sequence: Int,
    var timezone: String,
    var routeCode: String,
    var carrier: String,
    var fuelLiters: Double,
    var endsAt: Int,
    var priority: Int,
    var status: LegacyServicePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServicePlanMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServicePlanMappingStatus): Boolean {
        when (status) {
            LegacyServicePlanMappingStatus.NEW -> {
                when (next) {
                    LegacyServicePlanMappingStatus.QUEUED -> return true
                }
                return false
            }
            LegacyServicePlanMappingStatus.QUEUED -> return next == LegacyServicePlanMappingStatus.RUNNING || next == LegacyServicePlanMappingStatus.FAILED
            LegacyServicePlanMappingStatus.RUNNING -> return next == LegacyServicePlanMappingStatus.FAILED
            LegacyServicePlanMappingStatus.DONE -> {
                when (next) {
                    LegacyServicePlanMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyServicePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy service plan mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((endsAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): LegacyServicePlanMapping {
        return LegacyServicePlanMapping(
            code = code,
            title = title,
            sequence = sequence,
            timezone = timezone,
            routeCode = routeCode,
            carrier = carrier,
            fuelLiters = fuelLiters,
            endsAt = endsAt,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy service plan mapping in status new. */
fun newLegacyServicePlanMapping(code: String, title: String): LegacyServicePlanMapping {
    return LegacyServicePlanMapping(
        code = code,
        title = title,
        sequence = 0,
        timezone = "",
        routeCode = "",
        carrier = "",
        fuelLiters = 0.0,
        endsAt = 0,
        priority = 0,
        status = LegacyServicePlanMappingStatus.NEW,
    )
}
