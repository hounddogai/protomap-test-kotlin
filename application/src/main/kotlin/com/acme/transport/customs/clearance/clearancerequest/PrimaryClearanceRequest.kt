package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary clearance request records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a primary clearance request is in its life cycle. */
enum class PrimaryClearanceRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary clearance request. Its code is unique within a tenant. */
data class PrimaryClearanceRequest(
    var code: String,
    var label: String,
    var shiftCode: String,
    var discount: Double,
    var distanceKm: Double,
    var cost: Double,
    var approved: Boolean,
    var batchSize: Int,
    var status: PrimaryClearanceRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryClearanceRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryClearanceRequestStatus): Boolean {
        when (status) {
            PrimaryClearanceRequestStatus.PROPOSED -> {
                when (next) {
                    PrimaryClearanceRequestStatus.REJECTED, PrimaryClearanceRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryClearanceRequestStatus.APPROVED -> {
                when (next) {
                    PrimaryClearanceRequestStatus.REJECTED, PrimaryClearanceRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryClearanceRequestStatus.REJECTED -> return next == PrimaryClearanceRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryClearanceRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary clearance request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round(cost * distanceKm * 100) / 100

    fun snapshot(): PrimaryClearanceRequest {
        return PrimaryClearanceRequest(
            code = code,
            label = label,
            shiftCode = shiftCode,
            discount = discount,
            distanceKm = distanceKm,
            cost = cost,
            approved = approved,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary clearance request in status proposed. */
fun newPrimaryClearanceRequest(code: String, label: String): PrimaryClearanceRequest {
    return PrimaryClearanceRequest(
        code = code,
        label = label,
        shiftCode = "",
        discount = 0.0,
        distanceKm = 0.0,
        cost = 0.0,
        approved = false,
        batchSize = 0,
        status = PrimaryClearanceRequestStatus.PROPOSED,
    )
}
