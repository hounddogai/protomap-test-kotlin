package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary proof of delivery schedule is in its life cycle. */
enum class PrimaryProofOfDeliveryScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary proof of delivery schedule, with what the planners need to act on it. */
data class PrimaryProofOfDeliverySchedule(
    var code: String,
    var label: String,
    var shiftCode: String,
    var dock: String,
    var score: Double,
    var fuelLiters: Double,
    var region: String,
    var status: PrimaryProofOfDeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryProofOfDeliveryScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryProofOfDeliveryScheduleStatus): Boolean {
        when (status) {
            PrimaryProofOfDeliveryScheduleStatus.REQUESTED -> return next == PrimaryProofOfDeliveryScheduleStatus.REVOKED || next == PrimaryProofOfDeliveryScheduleStatus.EXPIRED
            PrimaryProofOfDeliveryScheduleStatus.GRANTED -> {
                when (next) {
                    PrimaryProofOfDeliveryScheduleStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryProofOfDeliveryScheduleStatus.REVOKED -> return next == PrimaryProofOfDeliveryScheduleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryProofOfDeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("primary proof of delivery schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((score - fuelLiters) * 100) / 100

    fun snapshot(): PrimaryProofOfDeliverySchedule {
        return PrimaryProofOfDeliverySchedule(
            code = code,
            label = label,
            shiftCode = shiftCode,
            dock = dock,
            score = score,
            fuelLiters = fuelLiters,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary proof of delivery schedule in status requested. */
fun newPrimaryProofOfDeliverySchedule(code: String, label: String): PrimaryProofOfDeliverySchedule {
    return PrimaryProofOfDeliverySchedule(
        code = code,
        label = label,
        shiftCode = "",
        dock = "",
        score = 0.0,
        fuelLiters = 0.0,
        region = "",
        status = PrimaryProofOfDeliveryScheduleStatus.REQUESTED,
    )
}
