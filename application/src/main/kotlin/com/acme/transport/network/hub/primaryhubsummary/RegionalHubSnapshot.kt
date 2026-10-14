package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional hub snapshot is in its life cycle. */
enum class RegionalHubSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional hub snapshot, with what the planners need to act on it. */
data class RegionalHubSnapshot(
    var code: String,
    var name: String,
    var pallets: Int,
    var visible: Boolean,
    var budget: Double,
    var status: RegionalHubSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalHubSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalHubSnapshotStatus): Boolean {
        when (status) {
            RegionalHubSnapshotStatus.NEW -> {
                when (next) {
                    RegionalHubSnapshotStatus.DONE, RegionalHubSnapshotStatus.FAILED -> return true
                }
                return false
            }
            RegionalHubSnapshotStatus.QUEUED -> return next == RegionalHubSnapshotStatus.DONE || next == RegionalHubSnapshotStatus.FAILED
            RegionalHubSnapshotStatus.RUNNING -> {
                when (next) {
                    RegionalHubSnapshotStatus.DONE -> return true
                }
                return false
            }
            RegionalHubSnapshotStatus.DONE -> return next == RegionalHubSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalHubSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional hub snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((pallets.toDouble() + budget) * 100) / 100

    fun snapshot(): RegionalHubSnapshot {
        return RegionalHubSnapshot(
            code = code,
            name = name,
            pallets = pallets,
            visible = visible,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional hub snapshot in status new. */
fun newRegionalHubSnapshot(code: String, name: String): RegionalHubSnapshot {
    return RegionalHubSnapshot(
        code = code,
        name = name,
        pallets = 0,
        visible = false,
        budget = 0.0,
        status = RegionalHubSnapshotStatus.NEW,
    )
}
