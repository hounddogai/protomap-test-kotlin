package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending proof of delivery plan is in its life cycle. */
enum class PendingProofOfDeliveryPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending proof of delivery plan as the proof of delivery team keeps it. */
data class PendingProofOfDeliveryPlan(
    var code: String,
    var title: String,
    var unit: String,
    var ratio: Double,
    var updatedAt: Int,
    var fuelLiters: Double,
    var createdAt: Int,
    var expiresAt: Int,
    var cost: Double,
    var status: PendingProofOfDeliveryPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingProofOfDeliveryPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingProofOfDeliveryPlanStatus): Boolean {
        when (status) {
            PendingProofOfDeliveryPlanStatus.OPEN -> {
                when (next) {
                    PendingProofOfDeliveryPlanStatus.REOPENED, PendingProofOfDeliveryPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingProofOfDeliveryPlanStatus.IN_REVIEW -> return next == PendingProofOfDeliveryPlanStatus.REOPENED || next == PendingProofOfDeliveryPlanStatus.ARCHIVED
            PendingProofOfDeliveryPlanStatus.RESOLVED -> {
                when (next) {
                    PendingProofOfDeliveryPlanStatus.REOPENED -> return true
                }
                return false
            }
            PendingProofOfDeliveryPlanStatus.REOPENED -> return next == PendingProofOfDeliveryPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingProofOfDeliveryPlanStatus) {
        if (!canMoveTo(next)) {
            error("pending proof of delivery plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending proof of delivery plan"} $code"

    fun projectedTotal(): Double = round((expiresAt.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): PendingProofOfDeliveryPlan {
        return PendingProofOfDeliveryPlan(
            code = code,
            title = title,
            unit = unit,
            ratio = ratio,
            updatedAt = updatedAt,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            expiresAt = expiresAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending proof of delivery plan in status open. */
fun newPendingProofOfDeliveryPlan(code: String, title: String): PendingProofOfDeliveryPlan {
    return PendingProofOfDeliveryPlan(
        code = code,
        title = title,
        unit = "",
        ratio = 0.0,
        updatedAt = 0,
        fuelLiters = 0.0,
        createdAt = 0,
        expiresAt = 0,
        cost = 0.0,
        status = PendingProofOfDeliveryPlanStatus.OPEN,
    )
}
