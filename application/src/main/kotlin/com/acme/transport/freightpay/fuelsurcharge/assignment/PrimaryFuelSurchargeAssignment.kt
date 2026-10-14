package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary fuel surcharge assignment records of freightpay's fuel surcharge work, and the rules, stores,
// and calculations around them.

/** Where a primary fuel surcharge assignment is in its life cycle. */
enum class PrimaryFuelSurchargeAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary fuel surcharge assignment, with what the planners need to act on it. */
data class PrimaryFuelSurchargeAssignment(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var visible: Boolean,
    var carrier: String,
    var endsAt: Int,
    var title: String,
    var windowDays: Int,
    var floorValue: Double,
    var checksum: String,
    var vehicleId: String,
    var status: PrimaryFuelSurchargeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelSurchargeAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelSurchargeAssignmentStatus): Boolean {
        when (status) {
            PrimaryFuelSurchargeAssignmentStatus.REQUESTED -> return next == PrimaryFuelSurchargeAssignmentStatus.REVOKED
            PrimaryFuelSurchargeAssignmentStatus.GRANTED -> return next == PrimaryFuelSurchargeAssignmentStatus.REVOKED || next == PrimaryFuelSurchargeAssignmentStatus.EXPIRED
            PrimaryFuelSurchargeAssignmentStatus.REVOKED -> return next == PrimaryFuelSurchargeAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryFuelSurchargeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel surcharge assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((endsAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): PrimaryFuelSurchargeAssignment {
        return PrimaryFuelSurchargeAssignment(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            visible = visible,
            carrier = carrier,
            endsAt = endsAt,
            title = title,
            windowDays = windowDays,
            floorValue = floorValue,
            checksum = checksum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary fuel surcharge assignment in status requested. */
fun newPrimaryFuelSurchargeAssignment(code: String, label: String): PrimaryFuelSurchargeAssignment {
    return PrimaryFuelSurchargeAssignment(
        code = code,
        label = label,
        fuelLiters = 0.0,
        visible = false,
        carrier = "",
        endsAt = 0,
        title = "",
        windowDays = 0,
        floorValue = 0.0,
        checksum = "",
        vehicleId = "",
        status = PrimaryFuelSurchargeAssignmentStatus.REQUESTED,
    )
}
