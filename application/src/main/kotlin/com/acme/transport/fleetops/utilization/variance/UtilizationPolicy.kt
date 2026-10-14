package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization policy is in its life cycle. */
enum class UtilizationPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One utilization policy, with what the planners need to act on it. */
data class UtilizationPolicy(
    var code: String,
    var title: String,
    var locale: String,
    var minimum: Double,
    var graceDays: Int,
    var trailerId: String,
    var status: UtilizationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationPolicyStatus): Boolean {
        when (status) {
            UtilizationPolicyStatus.CREATED -> {
                when (next) {
                    UtilizationPolicyStatus.RETIRED -> return true
                }
                return false
            }
            UtilizationPolicyStatus.VALIDATED -> return next == UtilizationPolicyStatus.PUBLISHED
            UtilizationPolicyStatus.PUBLISHED -> {
                when (next) {
                    UtilizationPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("utilization policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization policy"} $code"

    fun weightedValue(): Double = round((minimum - graceDays.toDouble()) * 100) / 100

    fun snapshot(): UtilizationPolicy {
        return UtilizationPolicy(
            code = code,
            title = title,
            locale = locale,
            minimum = minimum,
            graceDays = graceDays,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a utilization policy in status created. */
fun newUtilizationPolicy(code: String, title: String): UtilizationPolicy {
    return UtilizationPolicy(
        code = code,
        title = title,
        locale = "",
        minimum = 0.0,
        graceDays = 0,
        trailerId = "",
        status = UtilizationPolicyStatus.CREATED,
    )
}
