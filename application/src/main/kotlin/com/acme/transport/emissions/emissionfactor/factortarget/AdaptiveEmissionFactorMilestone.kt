package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive emission factor milestone is in its life cycle. */
enum class AdaptiveEmissionFactorMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive emission factor milestone as the emission factor team keeps it. */
data class AdaptiveEmissionFactorMilestone(
    var code: String,
    var title: String,
    var currency: String,
    var version: Int,
    var margin: Double,
    var status: AdaptiveEmissionFactorMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorMilestoneStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorMilestoneStatus.DRAFT -> {
                when (next) {
                    AdaptiveEmissionFactorMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorMilestoneStatus.PENDING -> return next == AdaptiveEmissionFactorMilestoneStatus.ACTIVE
            AdaptiveEmissionFactorMilestoneStatus.ACTIVE -> return next == AdaptiveEmissionFactorMilestoneStatus.CLOSED
            AdaptiveEmissionFactorMilestoneStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveEmissionFactorMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((version.toDouble() + margin) * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorMilestone {
        return AdaptiveEmissionFactorMilestone(
            code = code,
            title = title,
            currency = currency,
            version = version,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive emission factor milestone in status draft. */
fun newAdaptiveEmissionFactorMilestone(code: String, title: String): AdaptiveEmissionFactorMilestone {
    return AdaptiveEmissionFactorMilestone(
        code = code,
        title = title,
        currency = "",
        version = 0,
        margin = 0.0,
        status = AdaptiveEmissionFactorMilestoneStatus.DRAFT,
    )
}
