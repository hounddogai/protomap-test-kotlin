package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel surcharge assignment is in its life cycle. */
enum class LenientFuelSurchargeAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient fuel surcharge assignment, with what the planners need to act on it. */
data class LenientFuelSurchargeAssignment(
    var code: String,
    var label: String,
    var batchSize: Int,
    var threshold: Double,
    var rate: Double,
    var shiftCode: String,
    var depot: String,
    var reviewer: String,
    var windowDays: Int,
    var createdAt: Int,
    var loadKg: Double,
    var status: LenientFuelSurchargeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelSurchargeAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelSurchargeAssignmentStatus): Boolean {
        when (status) {
            LenientFuelSurchargeAssignmentStatus.REQUESTED -> {
                when (next) {
                    LenientFuelSurchargeAssignmentStatus.GRANTED, LenientFuelSurchargeAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            LenientFuelSurchargeAssignmentStatus.GRANTED -> return next == LenientFuelSurchargeAssignmentStatus.REVOKED
            LenientFuelSurchargeAssignmentStatus.REVOKED -> return next == LenientFuelSurchargeAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientFuelSurchargeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel surcharge assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((windowDays.toDouble() + loadKg) * 100) / 100

    fun snapshot(): LenientFuelSurchargeAssignment {
        return LenientFuelSurchargeAssignment(
            code = code,
            label = label,
            batchSize = batchSize,
            threshold = threshold,
            rate = rate,
            shiftCode = shiftCode,
            depot = depot,
            reviewer = reviewer,
            windowDays = windowDays,
            createdAt = createdAt,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient fuel surcharge assignment in status requested. */
fun newLenientFuelSurchargeAssignment(code: String, label: String): LenientFuelSurchargeAssignment {
    return LenientFuelSurchargeAssignment(
        code = code,
        label = label,
        batchSize = 0,
        threshold = 0.0,
        rate = 0.0,
        shiftCode = "",
        depot = "",
        reviewer = "",
        windowDays = 0,
        createdAt = 0,
        loadKg = 0.0,
        status = LenientFuelSurchargeAssignmentStatus.REQUESTED,
    )
}
