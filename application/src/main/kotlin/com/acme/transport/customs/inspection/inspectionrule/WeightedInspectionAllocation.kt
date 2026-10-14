package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted inspection allocation is in its life cycle. */
enum class WeightedInspectionAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted inspection allocation. Its code is unique within a tenant. */
data class WeightedInspectionAllocation(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var floorValue: Double,
    var description: String,
    var weight: Double,
    var approved: Boolean,
    var source: String,
    var checksum: String,
    var batchSize: Int,
    var status: WeightedInspectionAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionAllocationStatus): Boolean {
        when (status) {
            WeightedInspectionAllocationStatus.PLANNED -> {
                when (next) {
                    WeightedInspectionAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            WeightedInspectionAllocationStatus.SCHEDULED -> return next == WeightedInspectionAllocationStatus.CANCELLED
            WeightedInspectionAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedInspectionAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedInspectionAllocationStatus.COMPLETED -> return next == WeightedInspectionAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedInspectionAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round(batchSize.toDouble() * floorValue * 100) / 100

    fun snapshot(): WeightedInspectionAllocation {
        return WeightedInspectionAllocation(
            code = code,
            name = name,
            attributes = attributes,
            floorValue = floorValue,
            description = description,
            weight = weight,
            approved = approved,
            source = source,
            checksum = checksum,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted inspection allocation in status planned. */
fun newWeightedInspectionAllocation(code: String, name: String): WeightedInspectionAllocation {
    return WeightedInspectionAllocation(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        floorValue = 0.0,
        description = "",
        weight = 0.0,
        approved = false,
        source = "",
        checksum = "",
        batchSize = 0,
        status = WeightedInspectionAllocationStatus.PLANNED,
    )
}
