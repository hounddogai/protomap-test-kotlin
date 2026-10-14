package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.normalize

/** Where a relay estimate is in its life cycle. */
enum class RelayEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One relay estimate, with what the planners need to act on it. */
data class RelayEstimate(
    var code: String,
    var name: String,
    var dock: String,
    var amount: Double,
    var country: String,
    var status: RelayEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayEstimateStatus): Boolean {
        when (status) {
            RelayEstimateStatus.OPEN -> {
                when (next) {
                    RelayEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayEstimateStatus.IN_REVIEW -> {
                when (next) {
                    RelayEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayEstimateStatus.RESOLVED -> {
                when (next) {
                    RelayEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayEstimateStatus.REOPENED -> {
                when (next) {
                    RelayEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayEstimateStatus) {
        if (!canMoveTo(next)) {
            error("relay estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"relay estimate"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): RelayEstimate {
        return RelayEstimate(
            code = code,
            name = name,
            dock = dock,
            amount = amount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a relay estimate in status open. */
fun newRelayEstimate(code: String, name: String): RelayEstimate {
    return RelayEstimate(
        code = code,
        name = name,
        dock = "",
        amount = 0.0,
        country = "",
        status = RelayEstimateStatus.OPEN,
    )
}
