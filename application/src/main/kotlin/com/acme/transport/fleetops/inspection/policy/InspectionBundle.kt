package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection bundle is in its life cycle. */
enum class InspectionBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a inspection bundle and its current status. */
data class InspectionBundle(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var batchSize: Int,
    var segment: String,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var trailerId: String,
    var status: InspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBundleStatus): Boolean {
        when (status) {
            InspectionBundleStatus.OPEN -> return next == InspectionBundleStatus.IN_REVIEW || next == InspectionBundleStatus.REOPENED
            InspectionBundleStatus.IN_REVIEW -> {
                when (next) {
                    InspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            InspectionBundleStatus.RESOLVED -> return next == InspectionBundleStatus.REOPENED || next == InspectionBundleStatus.ARCHIVED
            InspectionBundleStatus.REOPENED -> {
                when (next) {
                    InspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("inspection bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((fuelLiters + batchSize.toDouble()) * 100) / 100

    fun snapshot(): InspectionBundle {
        return InspectionBundle(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            batchSize = batchSize,
            segment = segment,
            shiftCode = shiftCode,
            attributes = attributes,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a inspection bundle in status open. */
fun newInspectionBundle(code: String, name: String): InspectionBundle {
    return InspectionBundle(
        code = code,
        name = name,
        fuelLiters = 0.0,
        batchSize = 0,
        segment = "",
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        trailerId = "",
        status = InspectionBundleStatus.OPEN,
    )
}
