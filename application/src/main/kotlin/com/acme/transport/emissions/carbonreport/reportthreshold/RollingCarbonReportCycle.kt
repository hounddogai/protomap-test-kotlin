package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling carbon report cycle is in its life cycle. */
enum class RollingCarbonReportCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling carbon report cycle and its current status. */
data class RollingCarbonReportCycle(
    var code: String,
    var name: String,
    var country: String,
    var attributes: MutableMap<String, String>,
    var ratio: Double,
    var carrier: String,
    var currency: String,
    var minimum: Double,
    var ownerId: String,
    var status: RollingCarbonReportCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCarbonReportCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCarbonReportCycleStatus): Boolean {
        when (status) {
            RollingCarbonReportCycleStatus.DRAFT -> return next == RollingCarbonReportCycleStatus.SUSPENDED
            RollingCarbonReportCycleStatus.PENDING -> {
                when (next) {
                    RollingCarbonReportCycleStatus.SUSPENDED, RollingCarbonReportCycleStatus.CLOSED -> return true
                }
                return false
            }
            RollingCarbonReportCycleStatus.ACTIVE -> {
                when (next) {
                    RollingCarbonReportCycleStatus.CLOSED -> return true
                }
                return false
            }
            RollingCarbonReportCycleStatus.SUSPENDED -> return next == RollingCarbonReportCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingCarbonReportCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling carbon report cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((ratio + minimum) * 100) / 100

    fun snapshot(): RollingCarbonReportCycle {
        return RollingCarbonReportCycle(
            code = code,
            name = name,
            country = country,
            attributes = attributes,
            ratio = ratio,
            carrier = carrier,
            currency = currency,
            minimum = minimum,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling carbon report cycle in status draft. */
fun newRollingCarbonReportCycle(code: String, name: String): RollingCarbonReportCycle {
    return RollingCarbonReportCycle(
        code = code,
        name = name,
        country = "",
        attributes = mutableMapOf<String, String>(),
        ratio = 0.0,
        carrier = "",
        currency = "",
        minimum = 0.0,
        ownerId = "",
        status = RollingCarbonReportCycleStatus.DRAFT,
    )
}
