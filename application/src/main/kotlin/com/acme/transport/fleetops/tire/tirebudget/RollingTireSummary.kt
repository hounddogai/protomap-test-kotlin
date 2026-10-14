package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tire summary is in its life cycle. */
enum class RollingTireSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling tire summary, with what the planners need to act on it. */
data class RollingTireSummary(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var vehicleId: String,
    var currency: String,
    var amount: Double,
    var status: RollingTireSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTireSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTireSummaryStatus): Boolean {
        when (status) {
            RollingTireSummaryStatus.PROPOSED -> {
                when (next) {
                    RollingTireSummaryStatus.REJECTED -> return true
                }
                return false
            }
            RollingTireSummaryStatus.APPROVED -> {
                when (next) {
                    RollingTireSummaryStatus.REJECTED, RollingTireSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingTireSummaryStatus.REJECTED -> return next == RollingTireSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingTireSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rolling tire summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling tire summary"} $code"

    fun weightedTotal(): Double = round((amount + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RollingTireSummary {
        return RollingTireSummary(
            code = code,
            label = label,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            currency = currency,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling tire summary in status proposed. */
fun newRollingTireSummary(code: String, label: String): RollingTireSummary {
    return RollingTireSummary(
        code = code,
        label = label,
        updatedAt = 0,
        vehicleId = "",
        currency = "",
        amount = 0.0,
        status = RollingTireSummaryStatus.PROPOSED,
    )
}
