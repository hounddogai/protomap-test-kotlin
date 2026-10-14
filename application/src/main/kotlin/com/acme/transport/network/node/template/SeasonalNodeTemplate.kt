package com.acme.transport.network.node.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal node template records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a seasonal node template is in its life cycle. */
enum class SeasonalNodeTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal node template. Its code is unique within a tenant. */
data class SeasonalNodeTemplate(
    var code: String,
    var label: String,
    var odometer: Int,
    var pallets: Int,
    var carrier: String,
    var status: SeasonalNodeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNodeTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNodeTemplateStatus): Boolean {
        when (status) {
            SeasonalNodeTemplateStatus.DRAFT -> return next == SeasonalNodeTemplateStatus.SUSPENDED || next == SeasonalNodeTemplateStatus.CLOSED
            SeasonalNodeTemplateStatus.PENDING -> return next == SeasonalNodeTemplateStatus.ACTIVE || next == SeasonalNodeTemplateStatus.SUSPENDED
            SeasonalNodeTemplateStatus.ACTIVE -> return next == SeasonalNodeTemplateStatus.SUSPENDED || next == SeasonalNodeTemplateStatus.CLOSED
            SeasonalNodeTemplateStatus.SUSPENDED -> {
                when (next) {
                    SeasonalNodeTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalNodeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal node template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((odometer.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): SeasonalNodeTemplate {
        return SeasonalNodeTemplate(
            code = code,
            label = label,
            odometer = odometer,
            pallets = pallets,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal node template in status draft. */
fun newSeasonalNodeTemplate(code: String, label: String): SeasonalNodeTemplate {
    return SeasonalNodeTemplate(
        code = code,
        label = label,
        odometer = 0,
        pallets = 0,
        carrier = "",
        status = SeasonalNodeTemplateStatus.DRAFT,
    )
}
