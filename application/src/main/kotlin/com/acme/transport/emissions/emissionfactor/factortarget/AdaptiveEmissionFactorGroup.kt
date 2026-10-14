package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive emission factor group is in its life cycle. */
enum class AdaptiveEmissionFactorGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive emission factor group as the emission factor team keeps it. */
data class AdaptiveEmissionFactorGroup(
    var code: String,
    var label: String,
    var amount: Double,
    var quantity: Int,
    var rate: Double,
    var status: AdaptiveEmissionFactorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorGroupStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorGroupStatus.REQUESTED -> return next == AdaptiveEmissionFactorGroupStatus.EXPIRED
            AdaptiveEmissionFactorGroupStatus.GRANTED -> {
                when (next) {
                    AdaptiveEmissionFactorGroupStatus.REVOKED, AdaptiveEmissionFactorGroupStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorGroupStatus.REVOKED -> {
                when (next) {
                    AdaptiveEmissionFactorGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorGroupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive emission factor group"} $code"

    fun effectiveLoad(): Double = round(quantity.toDouble() * rate * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorGroup {
        return AdaptiveEmissionFactorGroup(
            code = code,
            label = label,
            amount = amount,
            quantity = quantity,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive emission factor group in status requested. */
fun newAdaptiveEmissionFactorGroup(code: String, label: String): AdaptiveEmissionFactorGroup {
    return AdaptiveEmissionFactorGroup(
        code = code,
        label = label,
        amount = 0.0,
        quantity = 0,
        rate = 0.0,
        status = AdaptiveEmissionFactorGroupStatus.REQUESTED,
    )
}
