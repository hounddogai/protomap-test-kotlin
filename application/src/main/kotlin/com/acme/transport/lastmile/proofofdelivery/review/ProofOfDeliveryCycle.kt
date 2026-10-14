package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery cycle is in its life cycle. */
enum class ProofOfDeliveryCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a proof of delivery cycle and its current status. */
data class ProofOfDeliveryCycle(
    var code: String,
    var title: String,
    var notes: String,
    var amount: Double,
    var ownerId: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var name: String,
    var unit: String,
    var status: ProofOfDeliveryCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryCycleStatus): Boolean {
        when (status) {
            ProofOfDeliveryCycleStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliveryCycleStatus.REJECTED, ProofOfDeliveryCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryCycleStatus.APPROVED -> return next == ProofOfDeliveryCycleStatus.REJECTED
            ProofOfDeliveryCycleStatus.REJECTED -> {
                when (next) {
                    ProofOfDeliveryCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryCycleStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((etaMinutes.toDouble() - amount) * 100) / 100

    fun snapshot(): ProofOfDeliveryCycle {
        return ProofOfDeliveryCycle(
            code = code,
            title = title,
            notes = notes,
            amount = amount,
            ownerId = ownerId,
            visible = visible,
            etaMinutes = etaMinutes,
            name = name,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a proof of delivery cycle in status proposed. */
fun newProofOfDeliveryCycle(code: String, title: String): ProofOfDeliveryCycle {
    return ProofOfDeliveryCycle(
        code = code,
        title = title,
        notes = "",
        amount = 0.0,
        ownerId = "",
        visible = false,
        etaMinutes = 0,
        name = "",
        unit = "",
        status = ProofOfDeliveryCycleStatus.PROPOSED,
    )
}
