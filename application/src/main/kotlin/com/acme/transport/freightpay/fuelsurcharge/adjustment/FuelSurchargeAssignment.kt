package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge assignment is in its life cycle. */
enum class FuelSurchargeAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fuel surcharge assignment and its current status. */
data class FuelSurchargeAssignment(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var version: Int,
    var sequence: Int,
    var active: Boolean,
    var region: String,
    var capacity: Int,
    var pallets: Int,
    var windowDays: Int,
    var status: FuelSurchargeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeAssignmentStatus): Boolean {
        when (status) {
            FuelSurchargeAssignmentStatus.OPEN -> {
                when (next) {
                    FuelSurchargeAssignmentStatus.IN_REVIEW -> return true
                }
                return false
            }
            FuelSurchargeAssignmentStatus.IN_REVIEW -> return next == FuelSurchargeAssignmentStatus.RESOLVED
            FuelSurchargeAssignmentStatus.RESOLVED -> {
                when (next) {
                    FuelSurchargeAssignmentStatus.REOPENED, FuelSurchargeAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeAssignmentStatus.REOPENED -> return next == FuelSurchargeAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round(pallets.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): FuelSurchargeAssignment {
        return FuelSurchargeAssignment(
            code = code,
            name = name,
            tags = tags,
            version = version,
            sequence = sequence,
            active = active,
            region = region,
            capacity = capacity,
            pallets = pallets,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel surcharge assignment in status open. */
fun newFuelSurchargeAssignment(code: String, name: String): FuelSurchargeAssignment {
    return FuelSurchargeAssignment(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        version = 0,
        sequence = 0,
        active = false,
        region = "",
        capacity = 0,
        pallets = 0,
        windowDays = 0,
        status = FuelSurchargeAssignmentStatus.OPEN,
    )
}
