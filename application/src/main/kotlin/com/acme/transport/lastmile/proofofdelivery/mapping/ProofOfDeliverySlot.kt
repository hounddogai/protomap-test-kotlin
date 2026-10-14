package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery slot is in its life cycle. */
enum class ProofOfDeliverySlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a proof of delivery slot and its current status. */
data class ProofOfDeliverySlot(
    var code: String,
    var label: String,
    var title: String,
    var routeCode: String,
    var name: String,
    var region: String,
    var floorValue: Double,
    var quantity: Int,
    var currency: String,
    var attempts: Int,
    var carrier: String,
    var status: ProofOfDeliverySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliverySlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliverySlotStatus): Boolean {
        when (status) {
            ProofOfDeliverySlotStatus.IDLE -> {
                when (next) {
                    ProofOfDeliverySlotStatus.DRAINING -> return true
                }
                return false
            }
            ProofOfDeliverySlotStatus.WARMING -> return next == ProofOfDeliverySlotStatus.READY || next == ProofOfDeliverySlotStatus.STOPPED
            ProofOfDeliverySlotStatus.READY -> {
                when (next) {
                    ProofOfDeliverySlotStatus.DRAINING, ProofOfDeliverySlotStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliverySlotStatus.DRAINING -> return next == ProofOfDeliverySlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliverySlotStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((quantity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliverySlot {
        return ProofOfDeliverySlot(
            code = code,
            label = label,
            title = title,
            routeCode = routeCode,
            name = name,
            region = region,
            floorValue = floorValue,
            quantity = quantity,
            currency = currency,
            attempts = attempts,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a proof of delivery slot in status idle. */
fun newProofOfDeliverySlot(code: String, label: String): ProofOfDeliverySlot {
    return ProofOfDeliverySlot(
        code = code,
        label = label,
        title = "",
        routeCode = "",
        name = "",
        region = "",
        floorValue = 0.0,
        quantity = 0,
        currency = "",
        attempts = 0,
        carrier = "",
        status = ProofOfDeliverySlotStatus.IDLE,
    )
}
