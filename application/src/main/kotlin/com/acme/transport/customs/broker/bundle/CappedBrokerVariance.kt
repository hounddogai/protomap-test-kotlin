package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped broker variance is in its life cycle. */
enum class CappedBrokerVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capped broker variance, with what the planners need to act on it. */
data class CappedBrokerVariance(
    var code: String,
    var name: String,
    var dock: String,
    var graceDays: Int,
    var cost: Double,
    var status: CappedBrokerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBrokerVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBrokerVarianceStatus): Boolean {
        when (status) {
            CappedBrokerVarianceStatus.NEW -> {
                when (next) {
                    CappedBrokerVarianceStatus.DONE -> return true
                }
                return false
            }
            CappedBrokerVarianceStatus.QUEUED -> {
                when (next) {
                    CappedBrokerVarianceStatus.RUNNING -> return true
                }
                return false
            }
            CappedBrokerVarianceStatus.RUNNING -> return next == CappedBrokerVarianceStatus.DONE || next == CappedBrokerVarianceStatus.FAILED
            CappedBrokerVarianceStatus.DONE -> return next == CappedBrokerVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedBrokerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("capped broker variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((graceDays.toDouble() + cost) * 100) / 100

    fun snapshot(): CappedBrokerVariance {
        return CappedBrokerVariance(
            code = code,
            name = name,
            dock = dock,
            graceDays = graceDays,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped broker variance in status new. */
fun newCappedBrokerVariance(code: String, name: String): CappedBrokerVariance {
    return CappedBrokerVariance(
        code = code,
        name = name,
        dock = "",
        graceDays = 0,
        cost = 0.0,
        status = CappedBrokerVarianceStatus.NEW,
    )
}
