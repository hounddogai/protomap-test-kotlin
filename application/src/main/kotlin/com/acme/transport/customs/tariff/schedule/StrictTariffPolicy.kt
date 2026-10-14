package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.normalize

/** Where a strict tariff policy is in its life cycle. */
enum class StrictTariffPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict tariff policy, with what the planners need to act on it. */
data class StrictTariffPolicy(
    var code: String,
    var label: String,
    var lane: String,
    var threshold: Double,
    var description: String,
    var status: StrictTariffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTariffPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTariffPolicyStatus): Boolean {
        when (status) {
            StrictTariffPolicyStatus.PROPOSED -> {
                when (next) {
                    StrictTariffPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictTariffPolicyStatus.APPROVED -> {
                when (next) {
                    StrictTariffPolicyStatus.REJECTED, StrictTariffPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictTariffPolicyStatus.REJECTED -> {
                when (next) {
                    StrictTariffPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTariffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict tariff policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = threshold

    fun snapshot(): StrictTariffPolicy {
        return StrictTariffPolicy(
            code = code,
            label = label,
            lane = lane,
            threshold = threshold,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict tariff policy in status proposed. */
fun newStrictTariffPolicy(code: String, label: String): StrictTariffPolicy {
    return StrictTariffPolicy(
        code = code,
        label = label,
        lane = "",
        threshold = 0.0,
        description = "",
        status = StrictTariffPolicyStatus.PROPOSED,
    )
}
