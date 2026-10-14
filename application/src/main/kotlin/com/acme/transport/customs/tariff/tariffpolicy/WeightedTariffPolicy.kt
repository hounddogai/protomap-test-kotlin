package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted tariff policy records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a weighted tariff policy is in its life cycle. */
enum class WeightedTariffPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted tariff policy, with what the planners need to act on it. */
data class WeightedTariffPolicy(
    var code: String,
    var name: String,
    var graceDays: Int,
    var floorValue: Double,
    var timezone: String,
    var attempts: Int,
    var archived: Boolean,
    var revision: Int,
    var status: WeightedTariffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTariffPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTariffPolicyStatus): Boolean {
        when (status) {
            WeightedTariffPolicyStatus.NEW -> return next == WeightedTariffPolicyStatus.FAILED
            WeightedTariffPolicyStatus.QUEUED -> return next == WeightedTariffPolicyStatus.RUNNING || next == WeightedTariffPolicyStatus.FAILED
            WeightedTariffPolicyStatus.RUNNING -> return next == WeightedTariffPolicyStatus.FAILED
            WeightedTariffPolicyStatus.DONE -> {
                when (next) {
                    WeightedTariffPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTariffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("weighted tariff policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"weighted tariff policy"} $code"

    fun netValue(): Double = round(graceDays.toDouble() * floorValue * 100) / 100

    fun snapshot(): WeightedTariffPolicy {
        return WeightedTariffPolicy(
            code = code,
            name = name,
            graceDays = graceDays,
            floorValue = floorValue,
            timezone = timezone,
            attempts = attempts,
            archived = archived,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted tariff policy in status new. */
fun newWeightedTariffPolicy(code: String, name: String): WeightedTariffPolicy {
    return WeightedTariffPolicy(
        code = code,
        name = name,
        graceDays = 0,
        floorValue = 0.0,
        timezone = "",
        attempts = 0,
        archived = false,
        revision = 0,
        status = WeightedTariffPolicyStatus.NEW,
    )
}
