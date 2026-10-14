package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery schedule is in its life cycle. */
enum class ProofOfDeliveryScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A proof of delivery schedule. Its code is unique within a tenant. */
data class ProofOfDeliverySchedule(
    var code: String,
    var name: String,
    var description: String,
    var locale: String,
    var quantity: Int,
    var visible: Boolean,
    var etaMinutes: Int,
    var status: ProofOfDeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryScheduleStatus): Boolean {
        when (status) {
            ProofOfDeliveryScheduleStatus.IDLE -> return next == ProofOfDeliveryScheduleStatus.DRAINING
            ProofOfDeliveryScheduleStatus.WARMING -> return next == ProofOfDeliveryScheduleStatus.DRAINING
            ProofOfDeliveryScheduleStatus.READY -> {
                when (next) {
                    ProofOfDeliveryScheduleStatus.DRAINING, ProofOfDeliveryScheduleStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliveryScheduleStatus.DRAINING -> {
                when (next) {
                    ProofOfDeliveryScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliverySchedule {
        return ProofOfDeliverySchedule(
            code = code,
            name = name,
            description = description,
            locale = locale,
            quantity = quantity,
            visible = visible,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a proof of delivery schedule in status idle. */
fun newProofOfDeliverySchedule(code: String, name: String): ProofOfDeliverySchedule {
    return ProofOfDeliverySchedule(
        code = code,
        name = name,
        description = "",
        locale = "",
        quantity = 0,
        visible = false,
        etaMinutes = 0,
        status = ProofOfDeliveryScheduleStatus.IDLE,
    )
}
