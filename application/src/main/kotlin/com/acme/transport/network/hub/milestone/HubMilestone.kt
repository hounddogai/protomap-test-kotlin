package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub milestone records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub milestone is in its life cycle. */
enum class HubMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a hub milestone and its current status. */
data class HubMilestone(
    var code: String,
    var title: String,
    var capacity: Int,
    var country: String,
    var routeCode: String,
    var driverId: String,
    var locale: String,
    var unit: String,
    var loadKg: Double,
    var pallets: Int,
    var batchSize: Int,
    var status: HubMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubMilestoneStatus): Boolean {
        when (status) {
            HubMilestoneStatus.REQUESTED -> return next == HubMilestoneStatus.GRANTED || next == HubMilestoneStatus.EXPIRED
            HubMilestoneStatus.GRANTED -> {
                when (next) {
                    HubMilestoneStatus.REVOKED, HubMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            HubMilestoneStatus.REVOKED -> return next == HubMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HubMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("hub milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hub milestone"} $code"

    fun netLoad(): Double = round((batchSize.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): HubMilestone {
        return HubMilestone(
            code = code,
            title = title,
            capacity = capacity,
            country = country,
            routeCode = routeCode,
            driverId = driverId,
            locale = locale,
            unit = unit,
            loadKg = loadKg,
            pallets = pallets,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a hub milestone in status requested. */
fun newHubMilestone(code: String, title: String): HubMilestone {
    return HubMilestone(
        code = code,
        title = title,
        capacity = 0,
        country = "",
        routeCode = "",
        driverId = "",
        locale = "",
        unit = "",
        loadKg = 0.0,
        pallets = 0,
        batchSize = 0,
        status = HubMilestoneStatus.REQUESTED,
    )
}
