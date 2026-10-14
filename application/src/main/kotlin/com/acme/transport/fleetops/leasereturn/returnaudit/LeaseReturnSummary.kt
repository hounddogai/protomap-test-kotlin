package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return summary is in its life cycle. */
enum class LeaseReturnSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lease return summary. Its code is unique within a tenant. */
data class LeaseReturnSummary(
    var code: String,
    var label: String,
    var region: String,
    var currency: String,
    var name: String,
    var sequence: Int,
    var expiresAt: Int,
    var version: Int,
    var locale: String,
    var floorValue: Double,
    var depot: String,
    var status: LeaseReturnSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnSummaryStatus): Boolean {
        when (status) {
            LeaseReturnSummaryStatus.IDLE -> {
                when (next) {
                    LeaseReturnSummaryStatus.READY -> return true
                }
                return false
            }
            LeaseReturnSummaryStatus.WARMING -> return next == LeaseReturnSummaryStatus.DRAINING || next == LeaseReturnSummaryStatus.STOPPED
            LeaseReturnSummaryStatus.READY -> return next == LeaseReturnSummaryStatus.DRAINING || next == LeaseReturnSummaryStatus.STOPPED
            LeaseReturnSummaryStatus.DRAINING -> return next == LeaseReturnSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LeaseReturnSummaryStatus) {
        if (!canMoveTo(next)) {
            error("lease return summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"lease return summary"} $code"

    fun adjustedExposure(): Double = round((expiresAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnSummary {
        return LeaseReturnSummary(
            code = code,
            label = label,
            region = region,
            currency = currency,
            name = name,
            sequence = sequence,
            expiresAt = expiresAt,
            version = version,
            locale = locale,
            floorValue = floorValue,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lease return summary in status idle. */
fun newLeaseReturnSummary(code: String, label: String): LeaseReturnSummary {
    return LeaseReturnSummary(
        code = code,
        label = label,
        region = "",
        currency = "",
        name = "",
        sequence = 0,
        expiresAt = 0,
        version = 0,
        locale = "",
        floorValue = 0.0,
        depot = "",
        status = LeaseReturnSummaryStatus.IDLE,
    )
}
