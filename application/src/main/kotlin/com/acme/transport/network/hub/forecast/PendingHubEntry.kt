package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hub entry is in its life cycle. */
enum class PendingHubEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending hub entry. Its code is unique within a tenant. */
data class PendingHubEntry(
    var code: String,
    var label: String,
    var name: String,
    var limit: Int,
    var trailerId: String,
    var region: String,
    var routeCode: String,
    var floorValue: Double,
    var status: PendingHubEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHubEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHubEntryStatus): Boolean {
        when (status) {
            PendingHubEntryStatus.CREATED -> {
                when (next) {
                    PendingHubEntryStatus.RETIRED -> return true
                }
                return false
            }
            PendingHubEntryStatus.VALIDATED -> {
                when (next) {
                    PendingHubEntryStatus.RETIRED -> return true
                }
                return false
            }
            PendingHubEntryStatus.PUBLISHED -> {
                when (next) {
                    PendingHubEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHubEntryStatus) {
        if (!canMoveTo(next)) {
            error("pending hub entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round(limit.toDouble() * floorValue * 100) / 100

    fun snapshot(): PendingHubEntry {
        return PendingHubEntry(
            code = code,
            label = label,
            name = name,
            limit = limit,
            trailerId = trailerId,
            region = region,
            routeCode = routeCode,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending hub entry in status created. */
fun newPendingHubEntry(code: String, label: String): PendingHubEntry {
    return PendingHubEntry(
        code = code,
        label = label,
        name = "",
        limit = 0,
        trailerId = "",
        region = "",
        routeCode = "",
        floorValue = 0.0,
        status = PendingHubEntryStatus.CREATED,
    )
}
