package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped near miss adjustment records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a capped near miss adjustment is in its life cycle. */
enum class CappedNearMissAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped near miss adjustment. Its code is unique within a tenant. */
data class CappedNearMissAdjustment(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var capacity: Int,
    var approved: Boolean,
    var contactEmail: String,
    var pallets: Int,
    var version: Int,
    var region: String,
    var loadKg: Double,
    var windowDays: Int,
    var status: CappedNearMissAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNearMissAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNearMissAdjustmentStatus): Boolean {
        when (status) {
            CappedNearMissAdjustmentStatus.CREATED -> {
                when (next) {
                    CappedNearMissAdjustmentStatus.VALIDATED, CappedNearMissAdjustmentStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedNearMissAdjustmentStatus.VALIDATED -> return next == CappedNearMissAdjustmentStatus.RETIRED
            CappedNearMissAdjustmentStatus.PUBLISHED -> return next == CappedNearMissAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedNearMissAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("capped near miss adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"capped near miss adjustment"} $code"

    fun adjustedLoad(): Double = round((version.toDouble() - loadKg) * 100) / 100

    fun snapshot(): CappedNearMissAdjustment {
        return CappedNearMissAdjustment(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            capacity = capacity,
            approved = approved,
            contactEmail = contactEmail,
            pallets = pallets,
            version = version,
            region = region,
            loadKg = loadKg,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped near miss adjustment in status created. */
fun newCappedNearMissAdjustment(code: String, label: String): CappedNearMissAdjustment {
    return CappedNearMissAdjustment(
        code = code,
        label = label,
        fuelLiters = 0.0,
        capacity = 0,
        approved = false,
        contactEmail = "",
        pallets = 0,
        version = 0,
        region = "",
        loadKg = 0.0,
        windowDays = 0,
        status = CappedNearMissAdjustmentStatus.CREATED,
    )
}
