package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default declaration mapping is in its life cycle. */
enum class DefaultDeclarationMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default declaration mapping, with what the planners need to act on it. */
data class DefaultDeclarationMapping(
    var code: String,
    var name: String,
    var retries: Int,
    var routeCode: String,
    var maximum: Double,
    var durationMinutes: Int,
    var lane: String,
    var etaMinutes: Int,
    var floorValue: Double,
    var status: DefaultDeclarationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDeclarationMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDeclarationMappingStatus): Boolean {
        when (status) {
            DefaultDeclarationMappingStatus.CREATED -> return next == DefaultDeclarationMappingStatus.VALIDATED || next == DefaultDeclarationMappingStatus.RETIRED
            DefaultDeclarationMappingStatus.VALIDATED -> {
                when (next) {
                    DefaultDeclarationMappingStatus.PUBLISHED, DefaultDeclarationMappingStatus.RETIRED -> return true
                }
                return false
            }
            DefaultDeclarationMappingStatus.PUBLISHED -> {
                when (next) {
                    DefaultDeclarationMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDeclarationMappingStatus) {
        if (!canMoveTo(next)) {
            error("default declaration mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() - floorValue) * 100) / 100

    fun snapshot(): DefaultDeclarationMapping {
        return DefaultDeclarationMapping(
            code = code,
            name = name,
            retries = retries,
            routeCode = routeCode,
            maximum = maximum,
            durationMinutes = durationMinutes,
            lane = lane,
            etaMinutes = etaMinutes,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default declaration mapping in status created. */
fun newDefaultDeclarationMapping(code: String, name: String): DefaultDeclarationMapping {
    return DefaultDeclarationMapping(
        code = code,
        name = name,
        retries = 0,
        routeCode = "",
        maximum = 0.0,
        durationMinutes = 0,
        lane = "",
        etaMinutes = 0,
        floorValue = 0.0,
        status = DefaultDeclarationMappingStatus.CREATED,
    )
}
