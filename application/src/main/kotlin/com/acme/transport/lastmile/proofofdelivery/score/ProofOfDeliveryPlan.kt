package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery plan is in its life cycle. */
enum class ProofOfDeliveryPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One proof of delivery plan, with what the planners need to act on it. */
data class ProofOfDeliveryPlan(
    var code: String,
    var title: String,
    var stopCount: Int,
    var dock: String,
    var floorValue: Double,
    var currency: String,
    var batchSize: Int,
    var weight: Double,
    var cost: Double,
    var priority: Int,
    var ratio: Double,
    var status: ProofOfDeliveryPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryPlanStatus): Boolean {
        when (status) {
            ProofOfDeliveryPlanStatus.REQUESTED -> {
                when (next) {
                    ProofOfDeliveryPlanStatus.REVOKED, ProofOfDeliveryPlanStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryPlanStatus.GRANTED -> return next == ProofOfDeliveryPlanStatus.REVOKED
            ProofOfDeliveryPlanStatus.REVOKED -> return next == ProofOfDeliveryPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryPlanStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((cost + priority.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryPlan {
        return ProofOfDeliveryPlan(
            code = code,
            title = title,
            stopCount = stopCount,
            dock = dock,
            floorValue = floorValue,
            currency = currency,
            batchSize = batchSize,
            weight = weight,
            cost = cost,
            priority = priority,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery plan in status requested. */
fun newProofOfDeliveryPlan(code: String, title: String): ProofOfDeliveryPlan {
    return ProofOfDeliveryPlan(
        code = code,
        title = title,
        stopCount = 0,
        dock = "",
        floorValue = 0.0,
        currency = "",
        batchSize = 0,
        weight = 0.0,
        cost = 0.0,
        priority = 0,
        ratio = 0.0,
        status = ProofOfDeliveryPlanStatus.REQUESTED,
    )
}
