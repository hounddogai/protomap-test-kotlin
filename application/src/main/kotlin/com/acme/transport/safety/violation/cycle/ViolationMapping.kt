package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation mapping is in its life cycle. */
enum class ViolationMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A violation mapping. Its code is unique within a tenant. */
data class ViolationMapping(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var minimum: Double,
    var version: Int,
    var summary: String,
    var status: ViolationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationMappingStatus): Boolean {
        when (status) {
            ViolationMappingStatus.DRAFT -> {
                when (next) {
                    ViolationMappingStatus.CLOSED -> return true
                }
                return false
            }
            ViolationMappingStatus.PENDING -> {
                when (next) {
                    ViolationMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            ViolationMappingStatus.ACTIVE -> return next == ViolationMappingStatus.SUSPENDED || next == ViolationMappingStatus.CLOSED
            ViolationMappingStatus.SUSPENDED -> return next == ViolationMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ViolationMappingStatus) {
        if (!canMoveTo(next)) {
            error("violation mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"violation mapping"} $code"

    fun weightedTotal(): Double = round(minimum * loadKg * 100) / 100

    fun snapshot(): ViolationMapping {
        return ViolationMapping(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            minimum = minimum,
            version = version,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation mapping in status draft. */
fun newViolationMapping(code: String, name: String): ViolationMapping {
    return ViolationMapping(
        code = code,
        name = name,
        durationMinutes = 0,
        loadKg = 0.0,
        minimum = 0.0,
        version = 0,
        summary = "",
        status = ViolationMappingStatus.DRAFT,
    )
}
