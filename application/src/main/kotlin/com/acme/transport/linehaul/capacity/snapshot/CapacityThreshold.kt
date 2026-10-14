package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.normalize

/** Where a capacity threshold is in its life cycle. */
enum class CapacityThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capacity threshold. Its code is unique within a tenant. */
data class CapacityThreshold(
    var code: String,
    var name: String,
    var region: String,
    var etaMinutes: Int,
    var notes: String,
    var status: CapacityThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityThresholdStatus): Boolean {
        when (status) {
            CapacityThresholdStatus.CREATED -> {
                when (next) {
                    CapacityThresholdStatus.VALIDATED, CapacityThresholdStatus.RETIRED -> return true
                }
                return false
            }
            CapacityThresholdStatus.VALIDATED -> {
                when (next) {
                    CapacityThresholdStatus.RETIRED -> return true
                }
                return false
            }
            CapacityThresholdStatus.PUBLISHED -> {
                when (next) {
                    CapacityThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capacity threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): CapacityThreshold {
        return CapacityThreshold(
            code = code,
            name = name,
            region = region,
            etaMinutes = etaMinutes,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capacity threshold in status created. */
fun newCapacityThreshold(code: String, name: String): CapacityThreshold {
    return CapacityThreshold(
        code = code,
        name = name,
        region = "",
        etaMinutes = 0,
        notes = "",
        status = CapacityThresholdStatus.CREATED,
    )
}
