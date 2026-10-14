package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary carbon report policy is in its life cycle. */
enum class PrimaryCarbonReportPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary carbon report policy as the carbon report team keeps it. */
data class PrimaryCarbonReportPolicy(
    var code: String,
    var name: String,
    var label: String,
    var retries: Int,
    var weight: Double,
    var odometer: Int,
    var windowDays: Int,
    var score: Double,
    var unit: String,
    var depot: String,
    var category: String,
    var status: PrimaryCarbonReportPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCarbonReportPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCarbonReportPolicyStatus): Boolean {
        when (status) {
            PrimaryCarbonReportPolicyStatus.PLANNED -> {
                when (next) {
                    PrimaryCarbonReportPolicyStatus.COMPLETED, PrimaryCarbonReportPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryCarbonReportPolicyStatus.SCHEDULED -> return next == PrimaryCarbonReportPolicyStatus.IN_PROGRESS
            PrimaryCarbonReportPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryCarbonReportPolicyStatus.COMPLETED, PrimaryCarbonReportPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryCarbonReportPolicyStatus.COMPLETED -> {
                when (next) {
                    PrimaryCarbonReportPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCarbonReportPolicyStatus) {
        if (!canMoveTo(next)) {
            error("primary carbon report policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary carbon report policy"} $code"

    fun adjustedLoad(): Double = round((windowDays.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCarbonReportPolicy {
        return PrimaryCarbonReportPolicy(
            code = code,
            name = name,
            label = label,
            retries = retries,
            weight = weight,
            odometer = odometer,
            windowDays = windowDays,
            score = score,
            unit = unit,
            depot = depot,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary carbon report policy in status planned. */
fun newPrimaryCarbonReportPolicy(code: String, name: String): PrimaryCarbonReportPolicy {
    return PrimaryCarbonReportPolicy(
        code = code,
        name = name,
        label = "",
        retries = 0,
        weight = 0.0,
        odometer = 0,
        windowDays = 0,
        score = 0.0,
        unit = "",
        depot = "",
        category = "",
        status = PrimaryCarbonReportPolicyStatus.PLANNED,
    )
}
