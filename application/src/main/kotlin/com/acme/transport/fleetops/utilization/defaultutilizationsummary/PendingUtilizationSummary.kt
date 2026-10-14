package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending utilization summary records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a pending utilization summary is in its life cycle. */
enum class PendingUtilizationSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending utilization summary and its current status. */
data class PendingUtilizationSummary(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var ratio: Double,
    var batchSize: Int,
    var active: Boolean,
    var floorValue: Double,
    var limit: Int,
    var region: String,
    var dock: String,
    var endsAt: Int,
    var status: PendingUtilizationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingUtilizationSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingUtilizationSummaryStatus): Boolean {
        when (status) {
            PendingUtilizationSummaryStatus.PROPOSED -> {
                when (next) {
                    PendingUtilizationSummaryStatus.REJECTED -> return true
                }
                return false
            }
            PendingUtilizationSummaryStatus.APPROVED -> return next == PendingUtilizationSummaryStatus.REJECTED || next == PendingUtilizationSummaryStatus.WITHDRAWN
            PendingUtilizationSummaryStatus.REJECTED -> {
                when (next) {
                    PendingUtilizationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingUtilizationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("pending utilization summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((fuelLiters + ratio) * 100) / 100

    fun snapshot(): PendingUtilizationSummary {
        return PendingUtilizationSummary(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            ratio = ratio,
            batchSize = batchSize,
            active = active,
            floorValue = floorValue,
            limit = limit,
            region = region,
            dock = dock,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending utilization summary in status proposed. */
fun newPendingUtilizationSummary(code: String, name: String): PendingUtilizationSummary {
    return PendingUtilizationSummary(
        code = code,
        name = name,
        fuelLiters = 0.0,
        ratio = 0.0,
        batchSize = 0,
        active = false,
        floorValue = 0.0,
        limit = 0,
        region = "",
        dock = "",
        endsAt = 0,
        status = PendingUtilizationSummaryStatus.PROPOSED,
    )
}
