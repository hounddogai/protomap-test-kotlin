package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional cross dock estimate records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a regional cross dock estimate is in its life cycle. */
enum class RegionalCrossDockEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional cross dock estimate and its current status. */
data class RegionalCrossDockEstimate(
    var code: String,
    var name: String,
    var phone: String,
    var rate: Double,
    var weight: Double,
    var startsAt: Int,
    var maximum: Double,
    var loadKg: Double,
    var segment: String,
    var status: RegionalCrossDockEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCrossDockEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCrossDockEstimateStatus): Boolean {
        when (status) {
            RegionalCrossDockEstimateStatus.PLANNED -> {
                when (next) {
                    RegionalCrossDockEstimateStatus.IN_PROGRESS, RegionalCrossDockEstimateStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalCrossDockEstimateStatus.SCHEDULED -> {
                when (next) {
                    RegionalCrossDockEstimateStatus.COMPLETED, RegionalCrossDockEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalCrossDockEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalCrossDockEstimateStatus.COMPLETED, RegionalCrossDockEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalCrossDockEstimateStatus.COMPLETED -> {
                when (next) {
                    RegionalCrossDockEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCrossDockEstimateStatus) {
        if (!canMoveTo(next)) {
            error("regional cross dock estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional cross dock estimate"} $code"

    fun adjustedValue(): Double = round((rate + maximum) * 100) / 100

    fun snapshot(): RegionalCrossDockEstimate {
        return RegionalCrossDockEstimate(
            code = code,
            name = name,
            phone = phone,
            rate = rate,
            weight = weight,
            startsAt = startsAt,
            maximum = maximum,
            loadKg = loadKg,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional cross dock estimate in status planned. */
fun newRegionalCrossDockEstimate(code: String, name: String): RegionalCrossDockEstimate {
    return RegionalCrossDockEstimate(
        code = code,
        name = name,
        phone = "",
        rate = 0.0,
        weight = 0.0,
        startsAt = 0,
        maximum = 0.0,
        loadKg = 0.0,
        segment = "",
        status = RegionalCrossDockEstimateStatus.PLANNED,
    )
}
