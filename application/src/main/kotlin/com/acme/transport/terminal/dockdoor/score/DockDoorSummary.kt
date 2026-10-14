package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door summary is in its life cycle. */
enum class DockDoorSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A dock door summary as the dock door team keeps it. */
data class DockDoorSummary(
    var code: String,
    var name: String,
    var vehicleId: String,
    var distanceKm: Double,
    var source: String,
    var score: Double,
    var graceDays: Int,
    var carrier: String,
    var status: DockDoorSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorSummaryStatus): Boolean {
        when (status) {
            DockDoorSummaryStatus.REQUESTED -> return next == DockDoorSummaryStatus.REVOKED || next == DockDoorSummaryStatus.EXPIRED
            DockDoorSummaryStatus.GRANTED -> return next == DockDoorSummaryStatus.REVOKED || next == DockDoorSummaryStatus.EXPIRED
            DockDoorSummaryStatus.REVOKED -> return next == DockDoorSummaryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DockDoorSummaryStatus) {
        if (!canMoveTo(next)) {
            error("dock door summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((graceDays.toDouble() + score) * 100) / 100

    fun snapshot(): DockDoorSummary {
        return DockDoorSummary(
            code = code,
            name = name,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            source = source,
            score = score,
            graceDays = graceDays,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dock door summary in status requested. */
fun newDockDoorSummary(code: String, name: String): DockDoorSummary {
    return DockDoorSummary(
        code = code,
        name = name,
        vehicleId = "",
        distanceKm = 0.0,
        source = "",
        score = 0.0,
        graceDays = 0,
        carrier = "",
        status = DockDoorSummaryStatus.REQUESTED,
    )
}
