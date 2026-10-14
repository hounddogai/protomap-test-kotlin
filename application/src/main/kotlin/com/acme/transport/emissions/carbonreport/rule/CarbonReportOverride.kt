package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.normalize

/** Where a carbon report override is in its life cycle. */
enum class CarbonReportOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A carbon report override as the carbon report team keeps it. */
data class CarbonReportOverride(
    var code: String,
    var name: String,
    var visible: Boolean,
    var reviewer: String,
    var budget: Double,
    var checksum: String,
    var status: CarbonReportOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportOverrideStatus): Boolean {
        when (status) {
            CarbonReportOverrideStatus.NEW -> {
                when (next) {
                    CarbonReportOverrideStatus.QUEUED, CarbonReportOverrideStatus.DONE -> return true
                }
                return false
            }
            CarbonReportOverrideStatus.QUEUED -> {
                when (next) {
                    CarbonReportOverrideStatus.RUNNING, CarbonReportOverrideStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportOverrideStatus.RUNNING -> {
                when (next) {
                    CarbonReportOverrideStatus.DONE, CarbonReportOverrideStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportOverrideStatus.DONE -> return next == CarbonReportOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CarbonReportOverrideStatus) {
        if (!canMoveTo(next)) {
            error("carbon report override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = budget

    fun snapshot(): CarbonReportOverride {
        return CarbonReportOverride(
            code = code,
            name = name,
            visible = visible,
            reviewer = reviewer,
            budget = budget,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a carbon report override in status new. */
fun newCarbonReportOverride(code: String, name: String): CarbonReportOverride {
    return CarbonReportOverride(
        code = code,
        name = name,
        visible = false,
        reviewer = "",
        budget = 0.0,
        checksum = "",
        status = CarbonReportOverrideStatus.NEW,
    )
}
