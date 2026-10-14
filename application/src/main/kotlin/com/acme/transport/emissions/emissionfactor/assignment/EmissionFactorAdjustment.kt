package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize

/** Where a emission factor adjustment is in its life cycle. */
enum class EmissionFactorAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A emission factor adjustment. Its code is unique within a tenant. */
data class EmissionFactorAdjustment(
    var code: String,
    var label: String,
    var summary: String,
    var margin: Double,
    var externalRef: String,
    var phone: String,
    var status: EmissionFactorAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorAdjustmentStatus): Boolean {
        when (status) {
            EmissionFactorAdjustmentStatus.REQUESTED -> return next == EmissionFactorAdjustmentStatus.GRANTED
            EmissionFactorAdjustmentStatus.GRANTED -> return next == EmissionFactorAdjustmentStatus.REVOKED || next == EmissionFactorAdjustmentStatus.EXPIRED
            EmissionFactorAdjustmentStatus.REVOKED -> {
                when (next) {
                    EmissionFactorAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("emission factor adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = margin

    fun snapshot(): EmissionFactorAdjustment {
        return EmissionFactorAdjustment(
            code = code,
            label = label,
            summary = summary,
            margin = margin,
            externalRef = externalRef,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor adjustment in status requested. */
fun newEmissionFactorAdjustment(code: String, label: String): EmissionFactorAdjustment {
    return EmissionFactorAdjustment(
        code = code,
        label = label,
        summary = "",
        margin = 0.0,
        externalRef = "",
        phone = "",
        status = EmissionFactorAdjustmentStatus.REQUESTED,
    )
}
