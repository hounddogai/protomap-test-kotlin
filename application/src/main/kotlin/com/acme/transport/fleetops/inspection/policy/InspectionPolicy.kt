package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection policy records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection policy is in its life cycle. */
enum class InspectionPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A inspection policy as the inspection team keeps it. */
data class InspectionPolicy(
    var code: String,
    var label: String,
    var rate: Double,
    var batchSize: Int,
    var distanceKm: Double,
    var contactEmail: String,
    var loadKg: Double,
    var floorValue: Double,
    var shiftCode: String,
    var capacity: Int,
    var status: InspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionPolicyStatus): Boolean {
        when (status) {
            InspectionPolicyStatus.IDLE -> return next == InspectionPolicyStatus.WARMING
            InspectionPolicyStatus.WARMING -> return next == InspectionPolicyStatus.READY || next == InspectionPolicyStatus.DRAINING
            InspectionPolicyStatus.READY -> return next == InspectionPolicyStatus.DRAINING || next == InspectionPolicyStatus.STOPPED
            InspectionPolicyStatus.DRAINING -> {
                when (next) {
                    InspectionPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("inspection policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round(rate * batchSize.toDouble() * 100) / 100

    fun snapshot(): InspectionPolicy {
        return InspectionPolicy(
            code = code,
            label = label,
            rate = rate,
            batchSize = batchSize,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            loadKg = loadKg,
            floorValue = floorValue,
            shiftCode = shiftCode,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection policy in status idle. */
fun newInspectionPolicy(code: String, label: String): InspectionPolicy {
    return InspectionPolicy(
        code = code,
        label = label,
        rate = 0.0,
        batchSize = 0,
        distanceKm = 0.0,
        contactEmail = "",
        loadKg = 0.0,
        floorValue = 0.0,
        shiftCode = "",
        capacity = 0,
        status = InspectionPolicyStatus.IDLE,
    )
}
