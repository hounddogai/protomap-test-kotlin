package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor summary is in its life cycle. */
enum class EmissionFactorSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A emission factor summary as the emission factor team keeps it. */
data class EmissionFactorSummary(
    var code: String,
    var title: String,
    var attempts: Int,
    var reviewer: String,
    var region: String,
    var sequence: Int,
    var ceiling: Double,
    var carrier: String,
    var summary: String,
    var contactEmail: String,
    var source: String,
    var status: EmissionFactorSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorSummaryStatus): Boolean {
        when (status) {
            EmissionFactorSummaryStatus.PLANNED -> return next == EmissionFactorSummaryStatus.IN_PROGRESS
            EmissionFactorSummaryStatus.SCHEDULED -> return next == EmissionFactorSummaryStatus.IN_PROGRESS
            EmissionFactorSummaryStatus.IN_PROGRESS -> return next == EmissionFactorSummaryStatus.COMPLETED
            EmissionFactorSummaryStatus.COMPLETED -> {
                when (next) {
                    EmissionFactorSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorSummaryStatus) {
        if (!canMoveTo(next)) {
            error("emission factor summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor summary"} $code"

    fun weightedExposure(): Double = round((sequence.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorSummary {
        return EmissionFactorSummary(
            code = code,
            title = title,
            attempts = attempts,
            reviewer = reviewer,
            region = region,
            sequence = sequence,
            ceiling = ceiling,
            carrier = carrier,
            summary = summary,
            contactEmail = contactEmail,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a emission factor summary in status planned. */
fun newEmissionFactorSummary(code: String, title: String): EmissionFactorSummary {
    return EmissionFactorSummary(
        code = code,
        title = title,
        attempts = 0,
        reviewer = "",
        region = "",
        sequence = 0,
        ceiling = 0.0,
        carrier = "",
        summary = "",
        contactEmail = "",
        source = "",
        status = EmissionFactorSummaryStatus.PLANNED,
    )
}
