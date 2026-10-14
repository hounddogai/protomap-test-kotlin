package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary service plan entry records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a primary service plan entry is in its life cycle. */
enum class PrimaryServicePlanEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary service plan entry as the service plan team keeps it. */
data class PrimaryServicePlanEntry(
    var code: String,
    var name: String,
    var visible: Boolean,
    var endsAt: Int,
    var pallets: Int,
    var maximum: Double,
    var dock: String,
    var channel: String,
    var pinned: Boolean,
    var status: PrimaryServicePlanEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanEntryStatus): Boolean {
        when (status) {
            PrimaryServicePlanEntryStatus.NEW -> return next == PrimaryServicePlanEntryStatus.RUNNING || next == PrimaryServicePlanEntryStatus.DONE
            PrimaryServicePlanEntryStatus.QUEUED -> {
                when (next) {
                    PrimaryServicePlanEntryStatus.DONE, PrimaryServicePlanEntryStatus.FAILED -> return true
                }
                return false
            }
            PrimaryServicePlanEntryStatus.RUNNING -> {
                when (next) {
                    PrimaryServicePlanEntryStatus.FAILED -> return true
                }
                return false
            }
            PrimaryServicePlanEntryStatus.DONE -> return next == PrimaryServicePlanEntryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round((maximum + endsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryServicePlanEntry {
        return PrimaryServicePlanEntry(
            code = code,
            name = name,
            visible = visible,
            endsAt = endsAt,
            pallets = pallets,
            maximum = maximum,
            dock = dock,
            channel = channel,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary service plan entry in status new. */
fun newPrimaryServicePlanEntry(code: String, name: String): PrimaryServicePlanEntry {
    return PrimaryServicePlanEntry(
        code = code,
        name = name,
        visible = false,
        endsAt = 0,
        pallets = 0,
        maximum = 0.0,
        dock = "",
        channel = "",
        pinned = false,
        status = PrimaryServicePlanEntryStatus.NEW,
    )
}
