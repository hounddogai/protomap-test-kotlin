package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted route offset record is in its life cycle. */
enum class WeightedRouteOffsetRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted route offset record as the route offset team keeps it. */
data class WeightedRouteOffsetRecord(
    var code: String,
    var label: String,
    var graceDays: Int,
    var version: Int,
    var checksum: String,
    var priority: Int,
    var unit: String,
    var channel: String,
    var fuelLiters: Double,
    var category: String,
    var status: WeightedRouteOffsetRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRouteOffsetRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRouteOffsetRecordStatus): Boolean {
        when (status) {
            WeightedRouteOffsetRecordStatus.IDLE -> {
                when (next) {
                    WeightedRouteOffsetRecordStatus.READY -> return true
                }
                return false
            }
            WeightedRouteOffsetRecordStatus.WARMING -> {
                when (next) {
                    WeightedRouteOffsetRecordStatus.STOPPED -> return true
                }
                return false
            }
            WeightedRouteOffsetRecordStatus.READY -> {
                when (next) {
                    WeightedRouteOffsetRecordStatus.STOPPED -> return true
                }
                return false
            }
            WeightedRouteOffsetRecordStatus.DRAINING -> {
                when (next) {
                    WeightedRouteOffsetRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRouteOffsetRecordStatus) {
        if (!canMoveTo(next)) {
            error("weighted route offset record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"weighted route offset record"} $code"

    fun effectiveLoad(): Double = round((priority.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): WeightedRouteOffsetRecord {
        return WeightedRouteOffsetRecord(
            code = code,
            label = label,
            graceDays = graceDays,
            version = version,
            checksum = checksum,
            priority = priority,
            unit = unit,
            channel = channel,
            fuelLiters = fuelLiters,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted route offset record in status idle. */
fun newWeightedRouteOffsetRecord(code: String, label: String): WeightedRouteOffsetRecord {
    return WeightedRouteOffsetRecord(
        code = code,
        label = label,
        graceDays = 0,
        version = 0,
        checksum = "",
        priority = 0,
        unit = "",
        channel = "",
        fuelLiters = 0.0,
        category = "",
        status = WeightedRouteOffsetRecordStatus.IDLE,
    )
}
