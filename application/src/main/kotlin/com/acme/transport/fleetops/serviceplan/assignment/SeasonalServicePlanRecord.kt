package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal service plan record is in its life cycle. */
enum class SeasonalServicePlanRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal service plan record. Its code is unique within a tenant. */
data class SeasonalServicePlanRecord(
    var code: String,
    var name: String,
    var checksum: String,
    var fuelLiters: Double,
    var carrier: String,
    var graceDays: Int,
    var lane: String,
    var maximum: Double,
    var stopCount: Int,
    var dock: String,
    var region: String,
    var status: SeasonalServicePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServicePlanRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServicePlanRecordStatus): Boolean {
        when (status) {
            SeasonalServicePlanRecordStatus.OPEN -> {
                when (next) {
                    SeasonalServicePlanRecordStatus.IN_REVIEW, SeasonalServicePlanRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalServicePlanRecordStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalServicePlanRecordStatus.RESOLVED, SeasonalServicePlanRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalServicePlanRecordStatus.RESOLVED -> return next == SeasonalServicePlanRecordStatus.REOPENED || next == SeasonalServicePlanRecordStatus.ARCHIVED
            SeasonalServicePlanRecordStatus.REOPENED -> return next == SeasonalServicePlanRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalServicePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service plan record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round(stopCount.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): SeasonalServicePlanRecord {
        return SeasonalServicePlanRecord(
            code = code,
            name = name,
            checksum = checksum,
            fuelLiters = fuelLiters,
            carrier = carrier,
            graceDays = graceDays,
            lane = lane,
            maximum = maximum,
            stopCount = stopCount,
            dock = dock,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal service plan record in status open. */
fun newSeasonalServicePlanRecord(code: String, name: String): SeasonalServicePlanRecord {
    return SeasonalServicePlanRecord(
        code = code,
        name = name,
        checksum = "",
        fuelLiters = 0.0,
        carrier = "",
        graceDays = 0,
        lane = "",
        maximum = 0.0,
        stopCount = 0,
        dock = "",
        region = "",
        status = SeasonalServicePlanRecordStatus.OPEN,
    )
}
