package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback inspection adjustment records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a fallback inspection adjustment is in its life cycle. */
enum class FallbackInspectionAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback inspection adjustment and its current status. */
data class FallbackInspectionAdjustment(
    var code: String,
    var name: String,
    var budget: Double,
    var durationMinutes: Int,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var cost: Double,
    var segment: String,
    var status: FallbackInspectionAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionAdjustmentStatus): Boolean {
        when (status) {
            FallbackInspectionAdjustmentStatus.CREATED -> {
                when (next) {
                    FallbackInspectionAdjustmentStatus.VALIDATED, FallbackInspectionAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
            FallbackInspectionAdjustmentStatus.VALIDATED -> {
                when (next) {
                    FallbackInspectionAdjustmentStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackInspectionAdjustmentStatus.PUBLISHED -> return next == FallbackInspectionAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackInspectionAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(durationMinutes.toDouble() * budget * 100) / 100

    fun snapshot(): FallbackInspectionAdjustment {
        return FallbackInspectionAdjustment(
            code = code,
            name = name,
            budget = budget,
            durationMinutes = durationMinutes,
            batchSize = batchSize,
            attributes = attributes,
            version = version,
            cost = cost,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback inspection adjustment in status created. */
fun newFallbackInspectionAdjustment(code: String, name: String): FallbackInspectionAdjustment {
    return FallbackInspectionAdjustment(
        code = code,
        name = name,
        budget = 0.0,
        durationMinutes = 0,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        version = 0,
        cost = 0.0,
        segment = "",
        status = FallbackInspectionAdjustmentStatus.CREATED,
    )
}
