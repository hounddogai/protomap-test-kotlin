package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.kit.text.normalize

/** Where a proof of delivery budget is in its life cycle. */
enum class ProofOfDeliveryBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A proof of delivery budget as the proof of delivery team keeps it. */
data class ProofOfDeliveryBudget(
    var code: String,
    var name: String,
    var score: Double,
    var trailerId: String,
    var carrier: String,
    var routeCode: String,
    var status: ProofOfDeliveryBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryBudgetStatus): Boolean {
        when (status) {
            ProofOfDeliveryBudgetStatus.DRAFT -> {
                when (next) {
                    ProofOfDeliveryBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            ProofOfDeliveryBudgetStatus.PENDING -> return next == ProofOfDeliveryBudgetStatus.ACTIVE || next == ProofOfDeliveryBudgetStatus.SUSPENDED
            ProofOfDeliveryBudgetStatus.ACTIVE -> return next == ProofOfDeliveryBudgetStatus.SUSPENDED || next == ProofOfDeliveryBudgetStatus.CLOSED
            ProofOfDeliveryBudgetStatus.SUSPENDED -> return next == ProofOfDeliveryBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryBudgetStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = score

    fun snapshot(): ProofOfDeliveryBudget {
        return ProofOfDeliveryBudget(
            code = code,
            name = name,
            score = score,
            trailerId = trailerId,
            carrier = carrier,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery budget in status draft. */
fun newProofOfDeliveryBudget(code: String, name: String): ProofOfDeliveryBudget {
    return ProofOfDeliveryBudget(
        code = code,
        name = name,
        score = 0.0,
        trailerId = "",
        carrier = "",
        routeCode = "",
        status = ProofOfDeliveryBudgetStatus.DRAFT,
    )
}
