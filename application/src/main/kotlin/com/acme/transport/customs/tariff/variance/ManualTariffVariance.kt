package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.text.normalize

// The manual tariff variance records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a manual tariff variance is in its life cycle. */
enum class ManualTariffVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a manual tariff variance and its current status. */
data class ManualTariffVariance(
    var code: String,
    var name: String,
    var version: Int,
    var tags: MutableList<String>,
    var active: Boolean,
    var status: ManualTariffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTariffVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTariffVarianceStatus): Boolean {
        when (status) {
            ManualTariffVarianceStatus.DRAFT -> {
                when (next) {
                    ManualTariffVarianceStatus.CLOSED -> return true
                }
                return false
            }
            ManualTariffVarianceStatus.PENDING -> {
                when (next) {
                    ManualTariffVarianceStatus.SUSPENDED, ManualTariffVarianceStatus.CLOSED -> return true
                }
                return false
            }
            ManualTariffVarianceStatus.ACTIVE -> return next == ManualTariffVarianceStatus.SUSPENDED
            ManualTariffVarianceStatus.SUSPENDED -> {
                when (next) {
                    ManualTariffVarianceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTariffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual tariff variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): ManualTariffVariance {
        return ManualTariffVariance(
            code = code,
            name = name,
            version = version,
            tags = tags,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual tariff variance in status draft. */
fun newManualTariffVariance(code: String, name: String): ManualTariffVariance {
    return ManualTariffVariance(
        code = code,
        name = name,
        version = 0,
        tags = mutableListOf<String>(),
        active = false,
        status = ManualTariffVarianceStatus.DRAFT,
    )
}
