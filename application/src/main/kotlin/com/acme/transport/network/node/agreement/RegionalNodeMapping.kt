package com.acme.transport.network.node.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional node mapping is in its life cycle. */
enum class RegionalNodeMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional node mapping. Its code is unique within a tenant. */
data class RegionalNodeMapping(
    var code: String,
    var name: String,
    var floorValue: Double,
    var routeCode: String,
    var ownerId: String,
    var budget: Double,
    var status: RegionalNodeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNodeMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNodeMappingStatus): Boolean {
        when (status) {
            RegionalNodeMappingStatus.DRAFT -> return next == RegionalNodeMappingStatus.PENDING
            RegionalNodeMappingStatus.PENDING -> {
                when (next) {
                    RegionalNodeMappingStatus.CLOSED -> return true
                }
                return false
            }
            RegionalNodeMappingStatus.ACTIVE -> return next == RegionalNodeMappingStatus.SUSPENDED || next == RegionalNodeMappingStatus.CLOSED
            RegionalNodeMappingStatus.SUSPENDED -> {
                when (next) {
                    RegionalNodeMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalNodeMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional node mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round((floorValue - budget) * 100) / 100

    fun snapshot(): RegionalNodeMapping {
        return RegionalNodeMapping(
            code = code,
            name = name,
            floorValue = floorValue,
            routeCode = routeCode,
            ownerId = ownerId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional node mapping in status draft. */
fun newRegionalNodeMapping(code: String, name: String): RegionalNodeMapping {
    return RegionalNodeMapping(
        code = code,
        name = name,
        floorValue = 0.0,
        routeCode = "",
        ownerId = "",
        budget = 0.0,
        status = RegionalNodeMappingStatus.DRAFT,
    )
}
