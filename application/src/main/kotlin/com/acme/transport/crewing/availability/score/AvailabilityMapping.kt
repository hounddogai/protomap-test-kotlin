package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.normalize

/** Where a availability mapping is in its life cycle. */
enum class AvailabilityMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One availability mapping, with what the planners need to act on it. */
data class AvailabilityMapping(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var currency: String,
    var notes: String,
    var quantity: Int,
    var status: AvailabilityMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityMappingStatus): Boolean {
        when (status) {
            AvailabilityMappingStatus.NEW -> {
                when (next) {
                    AvailabilityMappingStatus.RUNNING -> return true
                }
                return false
            }
            AvailabilityMappingStatus.QUEUED -> {
                when (next) {
                    AvailabilityMappingStatus.FAILED -> return true
                }
                return false
            }
            AvailabilityMappingStatus.RUNNING -> {
                when (next) {
                    AvailabilityMappingStatus.DONE, AvailabilityMappingStatus.FAILED -> return true
                }
                return false
            }
            AvailabilityMappingStatus.DONE -> return next == AvailabilityMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AvailabilityMappingStatus) {
        if (!canMoveTo(next)) {
            error("availability mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AvailabilityMapping {
        return AvailabilityMapping(
            code = code,
            name = name,
            pinned = pinned,
            currency = currency,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability mapping in status new. */
fun newAvailabilityMapping(code: String, name: String): AvailabilityMapping {
    return AvailabilityMapping(
        code = code,
        name = name,
        pinned = false,
        currency = "",
        notes = "",
        quantity = 0,
        status = AvailabilityMappingStatus.NEW,
    )
}
