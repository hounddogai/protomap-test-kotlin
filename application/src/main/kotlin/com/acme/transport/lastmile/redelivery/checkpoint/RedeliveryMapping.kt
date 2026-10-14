package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a redelivery mapping is in its life cycle. */
enum class RedeliveryMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A redelivery mapping as the redelivery team keeps it. */
data class RedeliveryMapping(
    var code: String,
    var name: String,
    var checksum: String,
    var notes: String,
    var maximum: Double,
    var unit: String,
    var status: RedeliveryMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryMappingStatus): Boolean {
        when (status) {
            RedeliveryMappingStatus.NEW -> {
                when (next) {
                    RedeliveryMappingStatus.RUNNING, RedeliveryMappingStatus.DONE -> return true
                }
                return false
            }
            RedeliveryMappingStatus.QUEUED -> return next == RedeliveryMappingStatus.RUNNING
            RedeliveryMappingStatus.RUNNING -> return next == RedeliveryMappingStatus.FAILED
            RedeliveryMappingStatus.DONE -> return next == RedeliveryMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RedeliveryMappingStatus) {
        if (!canMoveTo(next)) {
            error("redelivery mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = maximum

    fun snapshot(): RedeliveryMapping {
        return RedeliveryMapping(
            code = code,
            name = name,
            checksum = checksum,
            notes = notes,
            maximum = maximum,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a redelivery mapping in status new. */
fun newRedeliveryMapping(code: String, name: String): RedeliveryMapping {
    return RedeliveryMapping(
        code = code,
        name = name,
        checksum = "",
        notes = "",
        maximum = 0.0,
        unit = "",
        status = RedeliveryMappingStatus.NEW,
    )
}
