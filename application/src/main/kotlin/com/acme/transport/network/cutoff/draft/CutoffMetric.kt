package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff metric is in its life cycle. */
enum class CutoffMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff metric as the cutoff team keeps it. */
data class CutoffMetric(
    var code: String,
    var name: String,
    var source: String,
    var externalRef: String,
    var notes: String,
    var timezone: String,
    var contactEmail: String,
    var attempts: Int,
    var odometer: Int,
    var status: CutoffMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffMetricStatus): Boolean {
        when (status) {
            CutoffMetricStatus.NEW -> {
                when (next) {
                    CutoffMetricStatus.FAILED -> return true
                }
                return false
            }
            CutoffMetricStatus.QUEUED -> {
                when (next) {
                    CutoffMetricStatus.DONE -> return true
                }
                return false
            }
            CutoffMetricStatus.RUNNING -> return next == CutoffMetricStatus.FAILED
            CutoffMetricStatus.DONE -> return next == CutoffMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffMetricStatus) {
        if (!canMoveTo(next)) {
            error("cutoff metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((attempts.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): CutoffMetric {
        return CutoffMetric(
            code = code,
            name = name,
            source = source,
            externalRef = externalRef,
            notes = notes,
            timezone = timezone,
            contactEmail = contactEmail,
            attempts = attempts,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff metric in status new. */
fun newCutoffMetric(code: String, name: String): CutoffMetric {
    return CutoffMetric(
        code = code,
        name = name,
        source = "",
        externalRef = "",
        notes = "",
        timezone = "",
        contactEmail = "",
        attempts = 0,
        odometer = 0,
        status = CutoffMetricStatus.NEW,
    )
}
