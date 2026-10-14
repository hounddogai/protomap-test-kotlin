package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.kit.text.normalize

/** Where a rolling carbon report variance is in its life cycle. */
enum class RollingCarbonReportVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling carbon report variance. Its code is unique within a tenant. */
data class RollingCarbonReportVariance(
    var code: String,
    var label: String,
    var sequence: Int,
    var category: String,
    var reviewer: String,
    var segment: String,
    var depot: String,
    var contactEmail: String,
    var status: RollingCarbonReportVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCarbonReportVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCarbonReportVarianceStatus): Boolean {
        when (status) {
            RollingCarbonReportVarianceStatus.CREATED -> return next == RollingCarbonReportVarianceStatus.VALIDATED
            RollingCarbonReportVarianceStatus.VALIDATED -> {
                when (next) {
                    RollingCarbonReportVarianceStatus.PUBLISHED, RollingCarbonReportVarianceStatus.RETIRED -> return true
                }
                return false
            }
            RollingCarbonReportVarianceStatus.PUBLISHED -> return next == RollingCarbonReportVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingCarbonReportVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rolling carbon report variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): RollingCarbonReportVariance {
        return RollingCarbonReportVariance(
            code = code,
            label = label,
            sequence = sequence,
            category = category,
            reviewer = reviewer,
            segment = segment,
            depot = depot,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling carbon report variance in status created. */
fun newRollingCarbonReportVariance(code: String, label: String): RollingCarbonReportVariance {
    return RollingCarbonReportVariance(
        code = code,
        label = label,
        sequence = 0,
        category = "",
        reviewer = "",
        segment = "",
        depot = "",
        contactEmail = "",
        status = RollingCarbonReportVarianceStatus.CREATED,
    )
}
