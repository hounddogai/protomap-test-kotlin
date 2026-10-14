package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default carbon report assignment is in its life cycle. */
enum class DefaultCarbonReportAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default carbon report assignment. Its code is unique within a tenant. */
data class DefaultCarbonReportAssignment(
    var code: String,
    var label: String,
    var capacity: Int,
    var minimum: Double,
    var durationMinutes: Int,
    var stopCount: Int,
    var country: String,
    var locale: String,
    var status: DefaultCarbonReportAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCarbonReportAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCarbonReportAssignmentStatus): Boolean {
        when (status) {
            DefaultCarbonReportAssignmentStatus.CREATED -> {
                when (next) {
                    DefaultCarbonReportAssignmentStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultCarbonReportAssignmentStatus.VALIDATED -> return next == DefaultCarbonReportAssignmentStatus.PUBLISHED
            DefaultCarbonReportAssignmentStatus.PUBLISHED -> {
                when (next) {
                    DefaultCarbonReportAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCarbonReportAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default carbon report assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((minimum - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultCarbonReportAssignment {
        return DefaultCarbonReportAssignment(
            code = code,
            label = label,
            capacity = capacity,
            minimum = minimum,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            country = country,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default carbon report assignment in status created. */
fun newDefaultCarbonReportAssignment(code: String, label: String): DefaultCarbonReportAssignment {
    return DefaultCarbonReportAssignment(
        code = code,
        label = label,
        capacity = 0,
        minimum = 0.0,
        durationMinutes = 0,
        stopCount = 0,
        country = "",
        locale = "",
        status = DefaultCarbonReportAssignmentStatus.CREATED,
    )
}
