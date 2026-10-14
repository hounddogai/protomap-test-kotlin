package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped violation mapping is in its life cycle. */
enum class CappedViolationMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped violation mapping. Its code is unique within a tenant. */
data class CappedViolationMapping(
    var code: String,
    var title: String,
    var floorValue: Double,
    var sequence: Int,
    var rate: Double,
    var approved: Boolean,
    var status: CappedViolationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedViolationMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedViolationMappingStatus): Boolean {
        when (status) {
            CappedViolationMappingStatus.DRAFT -> return next == CappedViolationMappingStatus.ACTIVE || next == CappedViolationMappingStatus.CLOSED
            CappedViolationMappingStatus.PENDING -> return next == CappedViolationMappingStatus.SUSPENDED
            CappedViolationMappingStatus.ACTIVE -> {
                when (next) {
                    CappedViolationMappingStatus.SUSPENDED, CappedViolationMappingStatus.CLOSED -> return true
                }
                return false
            }
            CappedViolationMappingStatus.SUSPENDED -> return next == CappedViolationMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedViolationMappingStatus) {
        if (!canMoveTo(next)) {
            error("capped violation mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(sequence.toDouble() * floorValue * 100) / 100

    fun snapshot(): CappedViolationMapping {
        return CappedViolationMapping(
            code = code,
            title = title,
            floorValue = floorValue,
            sequence = sequence,
            rate = rate,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped violation mapping in status draft. */
fun newCappedViolationMapping(code: String, title: String): CappedViolationMapping {
    return CappedViolationMapping(
        code = code,
        title = title,
        floorValue = 0.0,
        sequence = 0,
        rate = 0.0,
        approved = false,
        status = CappedViolationMappingStatus.DRAFT,
    )
}
