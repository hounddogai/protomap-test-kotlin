package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor template is in its life cycle. */
enum class EmissionFactorTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A emission factor template. Its code is unique within a tenant. */
data class EmissionFactorTemplate(
    var code: String,
    var label: String,
    var channel: String,
    var pallets: Int,
    var endsAt: Int,
    var vehicleId: String,
    var threshold: Double,
    var updatedAt: Int,
    var visible: Boolean,
    var status: EmissionFactorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorTemplateStatus): Boolean {
        when (status) {
            EmissionFactorTemplateStatus.CREATED -> return next == EmissionFactorTemplateStatus.RETIRED
            EmissionFactorTemplateStatus.VALIDATED -> {
                when (next) {
                    EmissionFactorTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            EmissionFactorTemplateStatus.PUBLISHED -> return next == EmissionFactorTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: EmissionFactorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("emission factor template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"emission factor template"} $code"

    fun projectedValue(): Double = round((threshold - pallets.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorTemplate {
        return EmissionFactorTemplate(
            code = code,
            label = label,
            channel = channel,
            pallets = pallets,
            endsAt = endsAt,
            vehicleId = vehicleId,
            threshold = threshold,
            updatedAt = updatedAt,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a emission factor template in status created. */
fun newEmissionFactorTemplate(code: String, label: String): EmissionFactorTemplate {
    return EmissionFactorTemplate(
        code = code,
        label = label,
        channel = "",
        pallets = 0,
        endsAt = 0,
        vehicleId = "",
        threshold = 0.0,
        updatedAt = 0,
        visible = false,
        status = EmissionFactorTemplateStatus.CREATED,
    )
}
