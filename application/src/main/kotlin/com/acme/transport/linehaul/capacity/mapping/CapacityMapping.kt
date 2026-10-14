package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity mapping records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity mapping is in its life cycle. */
enum class CapacityMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capacity mapping. Its code is unique within a tenant. */
data class CapacityMapping(
    var code: String,
    var title: String,
    var country: String,
    var unit: String,
    var routeCode: String,
    var shiftCode: String,
    var archived: Boolean,
    var minimum: Double,
    var ceiling: Double,
    var createdAt: Int,
    var sequence: Int,
    var status: CapacityMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityMappingStatus): Boolean {
        when (status) {
            CapacityMappingStatus.PROPOSED -> return next == CapacityMappingStatus.WITHDRAWN
            CapacityMappingStatus.APPROVED -> {
                when (next) {
                    CapacityMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            CapacityMappingStatus.REJECTED -> {
                when (next) {
                    CapacityMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityMappingStatus) {
        if (!canMoveTo(next)) {
            error("capacity mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capacity mapping"} $code"

    fun projectedValue(): Double = round((minimum - ceiling) * 100) / 100

    fun snapshot(): CapacityMapping {
        return CapacityMapping(
            code = code,
            title = title,
            country = country,
            unit = unit,
            routeCode = routeCode,
            shiftCode = shiftCode,
            archived = archived,
            minimum = minimum,
            ceiling = ceiling,
            createdAt = createdAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capacity mapping in status proposed. */
fun newCapacityMapping(code: String, title: String): CapacityMapping {
    return CapacityMapping(
        code = code,
        title = title,
        country = "",
        unit = "",
        routeCode = "",
        shiftCode = "",
        archived = false,
        minimum = 0.0,
        ceiling = 0.0,
        createdAt = 0,
        sequence = 0,
        status = CapacityMappingStatus.PROPOSED,
    )
}
