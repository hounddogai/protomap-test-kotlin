package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.text.normalize

// The emission factor metric records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor metric is in its life cycle. */
enum class EmissionFactorMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A emission factor metric as the emission factor team keeps it. */
data class EmissionFactorMetric(
    var code: String,
    var label: String,
    var endsAt: Int,
    var phone: String,
    var ownerId: String,
    var driverId: String,
    var status: EmissionFactorMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorMetricStatus): Boolean {
        when (status) {
            EmissionFactorMetricStatus.DRAFT -> return next == EmissionFactorMetricStatus.SUSPENDED || next == EmissionFactorMetricStatus.CLOSED
            EmissionFactorMetricStatus.PENDING -> return next == EmissionFactorMetricStatus.SUSPENDED
            EmissionFactorMetricStatus.ACTIVE -> return next == EmissionFactorMetricStatus.SUSPENDED || next == EmissionFactorMetricStatus.CLOSED
            EmissionFactorMetricStatus.SUSPENDED -> {
                when (next) {
                    EmissionFactorMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorMetricStatus) {
        if (!canMoveTo(next)) {
            error("emission factor metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): EmissionFactorMetric {
        return EmissionFactorMetric(
            code = code,
            label = label,
            endsAt = endsAt,
            phone = phone,
            ownerId = ownerId,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor metric in status draft. */
fun newEmissionFactorMetric(code: String, label: String): EmissionFactorMetric {
    return EmissionFactorMetric(
        code = code,
        label = label,
        endsAt = 0,
        phone = "",
        ownerId = "",
        driverId = "",
        status = EmissionFactorMetricStatus.DRAFT,
    )
}
