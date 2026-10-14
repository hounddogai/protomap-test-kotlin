package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.text.normalize

/** Where a relay budget is in its life cycle. */
enum class RelayBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One relay budget, with what the planners need to act on it. */
data class RelayBudget(
    var code: String,
    var label: String,
    var region: String,
    var vehicleId: String,
    var attempts: Int,
    var status: RelayBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayBudgetStatus): Boolean {
        when (status) {
            RelayBudgetStatus.CREATED -> {
                when (next) {
                    RelayBudgetStatus.PUBLISHED -> return true
                }
                return false
            }
            RelayBudgetStatus.VALIDATED -> return next == RelayBudgetStatus.RETIRED
            RelayBudgetStatus.PUBLISHED -> return next == RelayBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RelayBudgetStatus) {
        if (!canMoveTo(next)) {
            error("relay budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"relay budget"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): RelayBudget {
        return RelayBudget(
            code = code,
            label = label,
            region = region,
            vehicleId = vehicleId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a relay budget in status created. */
fun newRelayBudget(code: String, label: String): RelayBudget {
    return RelayBudget(
        code = code,
        label = label,
        region = "",
        vehicleId = "",
        attempts = 0,
        status = RelayBudgetStatus.CREATED,
    )
}
