package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report group is in its life cycle. */
enum class CarbonReportGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A carbon report group. Its code is unique within a tenant. */
data class CarbonReportGroup(
    var code: String,
    var label: String,
    var attempts: Int,
    var minimum: Double,
    var weight: Double,
    var status: CarbonReportGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportGroupStatus): Boolean {
        when (status) {
            CarbonReportGroupStatus.NEW -> {
                when (next) {
                    CarbonReportGroupStatus.RUNNING, CarbonReportGroupStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportGroupStatus.QUEUED -> return next == CarbonReportGroupStatus.RUNNING || next == CarbonReportGroupStatus.DONE
            CarbonReportGroupStatus.RUNNING -> return next == CarbonReportGroupStatus.DONE || next == CarbonReportGroupStatus.FAILED
            CarbonReportGroupStatus.DONE -> return next == CarbonReportGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CarbonReportGroupStatus) {
        if (!canMoveTo(next)) {
            error("carbon report group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((weight - minimum) * 100) / 100

    fun snapshot(): CarbonReportGroup {
        return CarbonReportGroup(
            code = code,
            label = label,
            attempts = attempts,
            minimum = minimum,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a carbon report group in status new. */
fun newCarbonReportGroup(code: String, label: String): CarbonReportGroup {
    return CarbonReportGroup(
        code = code,
        label = label,
        attempts = 0,
        minimum = 0.0,
        weight = 0.0,
        status = CarbonReportGroupStatus.NEW,
    )
}
