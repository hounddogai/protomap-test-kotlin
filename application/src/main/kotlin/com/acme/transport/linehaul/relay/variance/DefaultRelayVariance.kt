package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.text.normalize

// The default relay variance records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a default relay variance is in its life cycle. */
enum class DefaultRelayVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default relay variance. Its code is unique within a tenant. */
data class DefaultRelayVariance(
    var code: String,
    var title: String,
    var channel: String,
    var unit: String,
    var category: String,
    var quantity: Int,
    var status: DefaultRelayVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRelayVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRelayVarianceStatus): Boolean {
        when (status) {
            DefaultRelayVarianceStatus.NEW -> return next == DefaultRelayVarianceStatus.RUNNING || next == DefaultRelayVarianceStatus.FAILED
            DefaultRelayVarianceStatus.QUEUED -> return next == DefaultRelayVarianceStatus.FAILED
            DefaultRelayVarianceStatus.RUNNING -> {
                when (next) {
                    DefaultRelayVarianceStatus.DONE -> return true
                }
                return false
            }
            DefaultRelayVarianceStatus.DONE -> return next == DefaultRelayVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultRelayVarianceStatus) {
        if (!canMoveTo(next)) {
            error("default relay variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DefaultRelayVariance {
        return DefaultRelayVariance(
            code = code,
            title = title,
            channel = channel,
            unit = unit,
            category = category,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default relay variance in status new. */
fun newDefaultRelayVariance(code: String, title: String): DefaultRelayVariance {
    return DefaultRelayVariance(
        code = code,
        title = title,
        channel = "",
        unit = "",
        category = "",
        quantity = 0,
        status = DefaultRelayVarianceStatus.NEW,
    )
}
