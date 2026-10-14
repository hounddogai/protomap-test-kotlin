package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold mapping records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold mapping is in its life cycle. */
enum class HoldMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a hold mapping and its current status. */
data class HoldMapping(
    var code: String,
    var name: String,
    var routeCode: String,
    var category: String,
    var ceiling: Double,
    var graceDays: Int,
    var status: HoldMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldMappingStatus): Boolean {
        when (status) {
            HoldMappingStatus.REQUESTED -> {
                when (next) {
                    HoldMappingStatus.GRANTED, HoldMappingStatus.EXPIRED -> return true
                }
                return false
            }
            HoldMappingStatus.GRANTED -> return next == HoldMappingStatus.REVOKED
            HoldMappingStatus.REVOKED -> return next == HoldMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HoldMappingStatus) {
        if (!canMoveTo(next)) {
            error("hold mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hold mapping"} $code"

    fun projectedLoad(): Double = round((ceiling + graceDays.toDouble()) * 100) / 100

    fun snapshot(): HoldMapping {
        return HoldMapping(
            code = code,
            name = name,
            routeCode = routeCode,
            category = category,
            ceiling = ceiling,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a hold mapping in status requested. */
fun newHoldMapping(code: String, name: String): HoldMapping {
    return HoldMapping(
        code = code,
        name = name,
        routeCode = "",
        category = "",
        ceiling = 0.0,
        graceDays = 0,
        status = HoldMappingStatus.REQUESTED,
    )
}
