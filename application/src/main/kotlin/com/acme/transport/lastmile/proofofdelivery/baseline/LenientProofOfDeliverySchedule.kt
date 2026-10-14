package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient proof of delivery schedule is in its life cycle. */
enum class LenientProofOfDeliveryScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient proof of delivery schedule and its current status. */
data class LenientProofOfDeliverySchedule(
    var code: String,
    var label: String,
    var discount: Double,
    var timezone: String,
    var startsAt: Int,
    var limit: Int,
    var checksum: String,
    var depot: String,
    var capacity: Int,
    var maximum: Double,
    var status: LenientProofOfDeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientProofOfDeliveryScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientProofOfDeliveryScheduleStatus): Boolean {
        when (status) {
            LenientProofOfDeliveryScheduleStatus.PLANNED -> return next == LenientProofOfDeliveryScheduleStatus.IN_PROGRESS || next == LenientProofOfDeliveryScheduleStatus.CANCELLED
            LenientProofOfDeliveryScheduleStatus.SCHEDULED -> {
                when (next) {
                    LenientProofOfDeliveryScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            LenientProofOfDeliveryScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    LenientProofOfDeliveryScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            LenientProofOfDeliveryScheduleStatus.COMPLETED -> {
                when (next) {
                    LenientProofOfDeliveryScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientProofOfDeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lenient proof of delivery schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((maximum + discount) * 100) / 100

    fun snapshot(): LenientProofOfDeliverySchedule {
        return LenientProofOfDeliverySchedule(
            code = code,
            label = label,
            discount = discount,
            timezone = timezone,
            startsAt = startsAt,
            limit = limit,
            checksum = checksum,
            depot = depot,
            capacity = capacity,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient proof of delivery schedule in status planned. */
fun newLenientProofOfDeliverySchedule(code: String, label: String): LenientProofOfDeliverySchedule {
    return LenientProofOfDeliverySchedule(
        code = code,
        label = label,
        discount = 0.0,
        timezone = "",
        startsAt = 0,
        limit = 0,
        checksum = "",
        depot = "",
        capacity = 0,
        maximum = 0.0,
        status = LenientProofOfDeliveryScheduleStatus.PLANNED,
    )
}
