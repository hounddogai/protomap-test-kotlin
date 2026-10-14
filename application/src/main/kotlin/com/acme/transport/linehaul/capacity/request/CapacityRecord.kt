package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.normalize

/** Where a capacity record is in its life cycle. */
enum class CapacityRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capacity record as the capacity team keeps it. */
data class CapacityRecord(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var minimum: Double,
    var source: String,
    var status: CapacityRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityRecordStatus): Boolean {
        when (status) {
            CapacityRecordStatus.OPEN -> return next == CapacityRecordStatus.IN_REVIEW
            CapacityRecordStatus.IN_REVIEW -> {
                when (next) {
                    CapacityRecordStatus.REOPENED -> return true
                }
                return false
            }
            CapacityRecordStatus.RESOLVED -> return next == CapacityRecordStatus.REOPENED || next == CapacityRecordStatus.ARCHIVED
            CapacityRecordStatus.REOPENED -> return next == CapacityRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CapacityRecordStatus) {
        if (!canMoveTo(next)) {
            error("capacity record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capacity record"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = minimum

    fun snapshot(): CapacityRecord {
        return CapacityRecord(
            code = code,
            name = name,
            attributes = attributes,
            vehicleId = vehicleId,
            minimum = minimum,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capacity record in status open. */
fun newCapacityRecord(code: String, name: String): CapacityRecord {
    return CapacityRecord(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        minimum = 0.0,
        source = "",
        status = CapacityRecordStatus.OPEN,
    )
}
