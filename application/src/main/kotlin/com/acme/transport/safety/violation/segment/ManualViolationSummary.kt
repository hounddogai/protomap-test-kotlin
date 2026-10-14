package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual violation summary is in its life cycle. */
enum class ManualViolationSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual violation summary, with what the planners need to act on it. */
data class ManualViolationSummary(
    var code: String,
    var name: String,
    var locale: String,
    var expiresAt: Int,
    var shiftCode: String,
    var threshold: Double,
    var vehicleId: String,
    var summary: String,
    var loadKg: Double,
    var maximum: Double,
    var status: ManualViolationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualViolationSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualViolationSummaryStatus): Boolean {
        when (status) {
            ManualViolationSummaryStatus.PLANNED -> return next == ManualViolationSummaryStatus.IN_PROGRESS
            ManualViolationSummaryStatus.SCHEDULED -> return next == ManualViolationSummaryStatus.IN_PROGRESS
            ManualViolationSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    ManualViolationSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            ManualViolationSummaryStatus.COMPLETED -> {
                when (next) {
                    ManualViolationSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualViolationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual violation summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((loadKg - maximum) * 100) / 100

    fun snapshot(): ManualViolationSummary {
        return ManualViolationSummary(
            code = code,
            name = name,
            locale = locale,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            threshold = threshold,
            vehicleId = vehicleId,
            summary = summary,
            loadKg = loadKg,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual violation summary in status planned. */
fun newManualViolationSummary(code: String, name: String): ManualViolationSummary {
    return ManualViolationSummary(
        code = code,
        name = name,
        locale = "",
        expiresAt = 0,
        shiftCode = "",
        threshold = 0.0,
        vehicleId = "",
        summary = "",
        loadKg = 0.0,
        maximum = 0.0,
        status = ManualViolationSummaryStatus.PLANNED,
    )
}
