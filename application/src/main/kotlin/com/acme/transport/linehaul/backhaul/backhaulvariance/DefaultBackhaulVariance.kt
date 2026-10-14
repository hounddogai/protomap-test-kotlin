package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.normalize

// The default backhaul variance records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a default backhaul variance is in its life cycle. */
enum class DefaultBackhaulVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default backhaul variance as the backhaul team keeps it. */
data class DefaultBackhaulVariance(
    var code: String,
    var name: String,
    var dock: String,
    var active: Boolean,
    var region: String,
    var summary: String,
    var threshold: Double,
    var country: String,
    var status: DefaultBackhaulVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBackhaulVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBackhaulVarianceStatus): Boolean {
        when (status) {
            DefaultBackhaulVarianceStatus.PROPOSED -> {
                when (next) {
                    DefaultBackhaulVarianceStatus.APPROVED, DefaultBackhaulVarianceStatus.REJECTED -> return true
                }
                return false
            }
            DefaultBackhaulVarianceStatus.APPROVED -> return next == DefaultBackhaulVarianceStatus.REJECTED || next == DefaultBackhaulVarianceStatus.WITHDRAWN
            DefaultBackhaulVarianceStatus.REJECTED -> {
                when (next) {
                    DefaultBackhaulVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultBackhaulVarianceStatus) {
        if (!canMoveTo(next)) {
            error("default backhaul variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default backhaul variance"} $code"

    fun measuredValue(): Double = threshold

    fun snapshot(): DefaultBackhaulVariance {
        return DefaultBackhaulVariance(
            code = code,
            name = name,
            dock = dock,
            active = active,
            region = region,
            summary = summary,
            threshold = threshold,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default backhaul variance in status proposed. */
fun newDefaultBackhaulVariance(code: String, name: String): DefaultBackhaulVariance {
    return DefaultBackhaulVariance(
        code = code,
        name = name,
        dock = "",
        active = false,
        region = "",
        summary = "",
        threshold = 0.0,
        country = "",
        status = DefaultBackhaulVarianceStatus.PROPOSED,
    )
}
