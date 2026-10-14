package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.text.normalize

// The adaptive emission factor variance records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a adaptive emission factor variance is in its life cycle. */
enum class AdaptiveEmissionFactorVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive emission factor variance. Its code is unique within a tenant. */
data class AdaptiveEmissionFactorVariance(
    var code: String,
    var label: String,
    var amount: Double,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var status: AdaptiveEmissionFactorVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorVarianceStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorVarianceStatus.IDLE -> {
                when (next) {
                    AdaptiveEmissionFactorVarianceStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorVarianceStatus.WARMING -> return next == AdaptiveEmissionFactorVarianceStatus.READY
            AdaptiveEmissionFactorVarianceStatus.READY -> {
                when (next) {
                    AdaptiveEmissionFactorVarianceStatus.DRAINING, AdaptiveEmissionFactorVarianceStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorVarianceStatus.DRAINING -> {
                when (next) {
                    AdaptiveEmissionFactorVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorVarianceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): AdaptiveEmissionFactorVariance {
        return AdaptiveEmissionFactorVariance(
            code = code,
            label = label,
            amount = amount,
            locale = locale,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive emission factor variance in status idle. */
fun newAdaptiveEmissionFactorVariance(code: String, label: String): AdaptiveEmissionFactorVariance {
    return AdaptiveEmissionFactorVariance(
        code = code,
        label = label,
        amount = 0.0,
        locale = "",
        attributes = mutableMapOf<String, String>(),
        status = AdaptiveEmissionFactorVarianceStatus.IDLE,
    )
}
