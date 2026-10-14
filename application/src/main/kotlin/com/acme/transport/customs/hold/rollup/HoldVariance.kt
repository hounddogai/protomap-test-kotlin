package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold variance is in its life cycle. */
enum class HoldVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A hold variance. Its code is unique within a tenant. */
data class HoldVariance(
    var code: String,
    var name: String,
    var loadKg: Double,
    var timezone: String,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var status: HoldVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldVarianceStatus): Boolean {
        when (status) {
            HoldVarianceStatus.OPEN -> return next == HoldVarianceStatus.RESOLVED || next == HoldVarianceStatus.REOPENED
            HoldVarianceStatus.IN_REVIEW -> return next == HoldVarianceStatus.REOPENED
            HoldVarianceStatus.RESOLVED -> return next == HoldVarianceStatus.REOPENED || next == HoldVarianceStatus.ARCHIVED
            HoldVarianceStatus.REOPENED -> return next == HoldVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: HoldVarianceStatus) {
        if (!canMoveTo(next)) {
            error("hold variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hold variance"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((cost - loadKg) * 100) / 100

    fun snapshot(): HoldVariance {
        return HoldVariance(
            code = code,
            name = name,
            loadKg = loadKg,
            timezone = timezone,
            cost = cost,
            attributes = attributes,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold variance in status open. */
fun newHoldVariance(code: String, name: String): HoldVariance {
    return HoldVariance(
        code = code,
        name = name,
        loadKg = 0.0,
        timezone = "",
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        approved = false,
        status = HoldVarianceStatus.OPEN,
    )
}
