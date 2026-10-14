package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary qualification override is in its life cycle. */
enum class PrimaryQualificationOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary qualification override and its current status. */
data class PrimaryQualificationOverride(
    var code: String,
    var label: String,
    var ceiling: Double,
    var tags: MutableList<String>,
    var ratio: Double,
    var fuelLiters: Double,
    var trailerId: String,
    var loadKg: Double,
    var vehicleId: String,
    var maximum: Double,
    var quantity: Int,
    var status: PrimaryQualificationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryQualificationOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryQualificationOverrideStatus): Boolean {
        when (status) {
            PrimaryQualificationOverrideStatus.PLANNED -> {
                when (next) {
                    PrimaryQualificationOverrideStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PrimaryQualificationOverrideStatus.SCHEDULED -> return next == PrimaryQualificationOverrideStatus.IN_PROGRESS || next == PrimaryQualificationOverrideStatus.COMPLETED
            PrimaryQualificationOverrideStatus.IN_PROGRESS -> return next == PrimaryQualificationOverrideStatus.COMPLETED
            PrimaryQualificationOverrideStatus.COMPLETED -> return next == PrimaryQualificationOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryQualificationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary qualification override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((ratio + maximum) * 100) / 100

    fun snapshot(): PrimaryQualificationOverride {
        return PrimaryQualificationOverride(
            code = code,
            label = label,
            ceiling = ceiling,
            tags = tags,
            ratio = ratio,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            loadKg = loadKg,
            vehicleId = vehicleId,
            maximum = maximum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary qualification override in status planned. */
fun newPrimaryQualificationOverride(code: String, label: String): PrimaryQualificationOverride {
    return PrimaryQualificationOverride(
        code = code,
        label = label,
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        ratio = 0.0,
        fuelLiters = 0.0,
        trailerId = "",
        loadKg = 0.0,
        vehicleId = "",
        maximum = 0.0,
        quantity = 0,
        status = PrimaryQualificationOverrideStatus.PLANNED,
    )
}
