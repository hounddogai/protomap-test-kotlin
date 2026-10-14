package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.normalize

/** Where a regional defect mapping is in its life cycle. */
enum class RegionalDefectMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional defect mapping as the defect team keeps it. */
data class RegionalDefectMapping(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var timezone: String,
    var shiftCode: String,
    var quantity: Int,
    var status: RegionalDefectMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectMappingStatus): Boolean {
        when (status) {
            RegionalDefectMappingStatus.PLANNED -> {
                when (next) {
                    RegionalDefectMappingStatus.COMPLETED, RegionalDefectMappingStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDefectMappingStatus.SCHEDULED -> return next == RegionalDefectMappingStatus.CANCELLED
            RegionalDefectMappingStatus.IN_PROGRESS -> return next == RegionalDefectMappingStatus.COMPLETED || next == RegionalDefectMappingStatus.CANCELLED
            RegionalDefectMappingStatus.COMPLETED -> {
                when (next) {
                    RegionalDefectMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDefectMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional defect mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional defect mapping"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RegionalDefectMapping {
        return RegionalDefectMapping(
            code = code,
            name = name,
            tags = tags,
            timezone = timezone,
            shiftCode = shiftCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional defect mapping in status planned. */
fun newRegionalDefectMapping(code: String, name: String): RegionalDefectMapping {
    return RegionalDefectMapping(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        timezone = "",
        shiftCode = "",
        quantity = 0,
        status = RegionalDefectMappingStatus.PLANNED,
    )
}
