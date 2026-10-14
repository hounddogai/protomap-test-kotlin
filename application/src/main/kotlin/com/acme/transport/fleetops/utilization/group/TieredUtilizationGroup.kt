package com.acme.transport.fleetops.utilization.group

import com.acme.transport.kit.text.normalize

// The tiered utilization group records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a tiered utilization group is in its life cycle. */
enum class TieredUtilizationGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered utilization group and its current status. */
data class TieredUtilizationGroup(
    var code: String,
    var label: String,
    var checksum: String,
    var routeCode: String,
    var segment: String,
    var score: Double,
    var status: TieredUtilizationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredUtilizationGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredUtilizationGroupStatus): Boolean {
        when (status) {
            TieredUtilizationGroupStatus.IDLE -> return next == TieredUtilizationGroupStatus.DRAINING
            TieredUtilizationGroupStatus.WARMING -> return next == TieredUtilizationGroupStatus.DRAINING
            TieredUtilizationGroupStatus.READY -> return next == TieredUtilizationGroupStatus.STOPPED
            TieredUtilizationGroupStatus.DRAINING -> return next == TieredUtilizationGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredUtilizationGroupStatus) {
        if (!canMoveTo(next)) {
            error("tiered utilization group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): TieredUtilizationGroup {
        return TieredUtilizationGroup(
            code = code,
            label = label,
            checksum = checksum,
            routeCode = routeCode,
            segment = segment,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered utilization group in status idle. */
fun newTieredUtilizationGroup(code: String, label: String): TieredUtilizationGroup {
    return TieredUtilizationGroup(
        code = code,
        label = label,
        checksum = "",
        routeCode = "",
        segment = "",
        score = 0.0,
        status = TieredUtilizationGroupStatus.IDLE,
    )
}
