package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional carbon report group records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a regional carbon report group is in its life cycle. */
enum class RegionalCarbonReportGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional carbon report group and its current status. */
data class RegionalCarbonReportGroup(
    var code: String,
    var name: String,
    var currency: String,
    var minimum: Double,
    var label: String,
    var windowDays: Int,
    var version: Int,
    var amount: Double,
    var sequence: Int,
    var status: RegionalCarbonReportGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportGroupStatus): Boolean {
        when (status) {
            RegionalCarbonReportGroupStatus.IDLE -> return next == RegionalCarbonReportGroupStatus.DRAINING
            RegionalCarbonReportGroupStatus.WARMING -> {
                when (next) {
                    RegionalCarbonReportGroupStatus.READY -> return true
                }
                return false
            }
            RegionalCarbonReportGroupStatus.READY -> {
                when (next) {
                    RegionalCarbonReportGroupStatus.DRAINING -> return true
                }
                return false
            }
            RegionalCarbonReportGroupStatus.DRAINING -> {
                when (next) {
                    RegionalCarbonReportGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportGroupStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report group $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$name]"

    fun projectedValue(): Double = round((windowDays.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RegionalCarbonReportGroup {
        return RegionalCarbonReportGroup(
            code = code,
            name = name,
            currency = currency,
            minimum = minimum,
            label = label,
            windowDays = windowDays,
            version = version,
            amount = amount,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional carbon report group in status idle. */
fun newRegionalCarbonReportGroup(code: String, name: String): RegionalCarbonReportGroup {
    return RegionalCarbonReportGroup(
        code = code,
        name = name,
        currency = "",
        minimum = 0.0,
        label = "",
        windowDays = 0,
        version = 0,
        amount = 0.0,
        sequence = 0,
        status = RegionalCarbonReportGroupStatus.IDLE,
    )
}
