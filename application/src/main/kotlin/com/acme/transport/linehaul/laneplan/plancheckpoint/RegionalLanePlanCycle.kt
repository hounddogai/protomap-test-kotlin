package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional lane plan cycle is in its life cycle. */
enum class RegionalLanePlanCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a regional lane plan cycle and its current status. */
data class RegionalLanePlanCycle(
    var code: String,
    var name: String,
    var driverId: String,
    var threshold: Double,
    var tags: MutableList<String>,
    var checksum: String,
    var pinned: Boolean,
    var visible: Boolean,
    var channel: String,
    var floorValue: Double,
    var title: String,
    var status: RegionalLanePlanCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLanePlanCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLanePlanCycleStatus): Boolean {
        when (status) {
            RegionalLanePlanCycleStatus.DRAFT -> {
                when (next) {
                    RegionalLanePlanCycleStatus.ACTIVE, RegionalLanePlanCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalLanePlanCycleStatus.PENDING -> return next == RegionalLanePlanCycleStatus.ACTIVE || next == RegionalLanePlanCycleStatus.SUSPENDED
            RegionalLanePlanCycleStatus.ACTIVE -> return next == RegionalLanePlanCycleStatus.SUSPENDED
            RegionalLanePlanCycleStatus.SUSPENDED -> return next == RegionalLanePlanCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalLanePlanCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional lane plan cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((floorValue - threshold) * 100) / 100

    fun snapshot(): RegionalLanePlanCycle {
        return RegionalLanePlanCycle(
            code = code,
            name = name,
            driverId = driverId,
            threshold = threshold,
            tags = tags,
            checksum = checksum,
            pinned = pinned,
            visible = visible,
            channel = channel,
            floorValue = floorValue,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional lane plan cycle in status draft. */
fun newRegionalLanePlanCycle(code: String, name: String): RegionalLanePlanCycle {
    return RegionalLanePlanCycle(
        code = code,
        name = name,
        driverId = "",
        threshold = 0.0,
        tags = mutableListOf<String>(),
        checksum = "",
        pinned = false,
        visible = false,
        channel = "",
        floorValue = 0.0,
        title = "",
        status = RegionalLanePlanCycleStatus.DRAFT,
    )
}
