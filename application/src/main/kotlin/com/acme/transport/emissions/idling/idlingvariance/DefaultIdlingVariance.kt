package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default idling variance records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a default idling variance is in its life cycle. */
enum class DefaultIdlingVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One default idling variance, with what the planners need to act on it. */
data class DefaultIdlingVariance(
    var code: String,
    var name: String,
    var score: Double,
    var contactEmail: String,
    var batchSize: Int,
    var cost: Double,
    var stopCount: Int,
    var status: DefaultIdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIdlingVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIdlingVarianceStatus): Boolean {
        when (status) {
            DefaultIdlingVarianceStatus.PROPOSED -> return next == DefaultIdlingVarianceStatus.REJECTED
            DefaultIdlingVarianceStatus.APPROVED -> return next == DefaultIdlingVarianceStatus.WITHDRAWN
            DefaultIdlingVarianceStatus.REJECTED -> return next == DefaultIdlingVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultIdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("default idling variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((cost - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DefaultIdlingVariance {
        return DefaultIdlingVariance(
            code = code,
            name = name,
            score = score,
            contactEmail = contactEmail,
            batchSize = batchSize,
            cost = cost,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default idling variance in status proposed. */
fun newDefaultIdlingVariance(code: String, name: String): DefaultIdlingVariance {
    return DefaultIdlingVariance(
        code = code,
        name = name,
        score = 0.0,
        contactEmail = "",
        batchSize = 0,
        cost = 0.0,
        stopCount = 0,
        status = DefaultIdlingVarianceStatus.PROPOSED,
    )
}
