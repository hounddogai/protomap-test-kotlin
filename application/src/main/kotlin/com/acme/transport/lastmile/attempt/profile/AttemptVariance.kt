package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.kit.text.normalize

/** Where a attempt variance is in its life cycle. */
enum class AttemptVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One attempt variance, with what the planners need to act on it. */
data class AttemptVariance(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var locale: String,
    var driverId: String,
    var category: String,
    var quantity: Int,
    var status: AttemptVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptVarianceStatus): Boolean {
        when (status) {
            AttemptVarianceStatus.PROPOSED -> return next == AttemptVarianceStatus.WITHDRAWN
            AttemptVarianceStatus.APPROVED -> return next == AttemptVarianceStatus.REJECTED || next == AttemptVarianceStatus.WITHDRAWN
            AttemptVarianceStatus.REJECTED -> return next == AttemptVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AttemptVarianceStatus) {
        if (!canMoveTo(next)) {
            error("attempt variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AttemptVariance {
        return AttemptVariance(
            code = code,
            name = name,
            attributes = attributes,
            locale = locale,
            driverId = driverId,
            category = category,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a attempt variance in status proposed. */
fun newAttemptVariance(code: String, name: String): AttemptVariance {
    return AttemptVariance(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        locale = "",
        driverId = "",
        category = "",
        quantity = 0,
        status = AttemptVarianceStatus.PROPOSED,
    )
}
