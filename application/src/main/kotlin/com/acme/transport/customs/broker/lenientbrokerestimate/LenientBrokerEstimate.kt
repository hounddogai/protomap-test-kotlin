package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.normalize

// The lenient broker estimate records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a lenient broker estimate is in its life cycle. */
enum class LenientBrokerEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient broker estimate as the broker team keeps it. */
data class LenientBrokerEstimate(
    var code: String,
    var title: String,
    var reviewer: String,
    var source: String,
    var cost: Double,
    var status: LenientBrokerEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBrokerEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBrokerEstimateStatus): Boolean {
        when (status) {
            LenientBrokerEstimateStatus.REQUESTED -> {
                when (next) {
                    LenientBrokerEstimateStatus.GRANTED, LenientBrokerEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            LenientBrokerEstimateStatus.GRANTED -> {
                when (next) {
                    LenientBrokerEstimateStatus.REVOKED, LenientBrokerEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            LenientBrokerEstimateStatus.REVOKED -> {
                when (next) {
                    LenientBrokerEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientBrokerEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient broker estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = cost

    fun snapshot(): LenientBrokerEstimate {
        return LenientBrokerEstimate(
            code = code,
            title = title,
            reviewer = reviewer,
            source = source,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient broker estimate in status requested. */
fun newLenientBrokerEstimate(code: String, title: String): LenientBrokerEstimate {
    return LenientBrokerEstimate(
        code = code,
        title = title,
        reviewer = "",
        source = "",
        cost = 0.0,
        status = LenientBrokerEstimateStatus.REQUESTED,
    )
}
