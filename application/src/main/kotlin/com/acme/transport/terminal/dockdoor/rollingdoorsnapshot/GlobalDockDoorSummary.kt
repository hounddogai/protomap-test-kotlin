package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dock door summary is in its life cycle. */
enum class GlobalDockDoorSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global dock door summary as the dock door team keeps it. */
data class GlobalDockDoorSummary(
    var code: String,
    var label: String,
    var pallets: Int,
    var region: String,
    var stopCount: Int,
    var windowDays: Int,
    var contactEmail: String,
    var batchSize: Int,
    var source: String,
    var status: GlobalDockDoorSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDockDoorSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDockDoorSummaryStatus): Boolean {
        when (status) {
            GlobalDockDoorSummaryStatus.PROPOSED -> return next == GlobalDockDoorSummaryStatus.APPROVED || next == GlobalDockDoorSummaryStatus.WITHDRAWN
            GlobalDockDoorSummaryStatus.APPROVED -> return next == GlobalDockDoorSummaryStatus.REJECTED
            GlobalDockDoorSummaryStatus.REJECTED -> {
                when (next) {
                    GlobalDockDoorSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDockDoorSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global dock door summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((stopCount.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): GlobalDockDoorSummary {
        return GlobalDockDoorSummary(
            code = code,
            label = label,
            pallets = pallets,
            region = region,
            stopCount = stopCount,
            windowDays = windowDays,
            contactEmail = contactEmail,
            batchSize = batchSize,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global dock door summary in status proposed. */
fun newGlobalDockDoorSummary(code: String, label: String): GlobalDockDoorSummary {
    return GlobalDockDoorSummary(
        code = code,
        label = label,
        pallets = 0,
        region = "",
        stopCount = 0,
        windowDays = 0,
        contactEmail = "",
        batchSize = 0,
        source = "",
        status = GlobalDockDoorSummaryStatus.PROPOSED,
    )
}
