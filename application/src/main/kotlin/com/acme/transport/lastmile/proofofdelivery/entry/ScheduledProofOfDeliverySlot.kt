package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled proof of delivery slot is in its life cycle. */
enum class ScheduledProofOfDeliverySlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled proof of delivery slot and its current status. */
data class ScheduledProofOfDeliverySlot(
    var code: String,
    var name: String,
    var visible: Boolean,
    var country: String,
    var checksum: String,
    var stopCount: Int,
    var summary: String,
    var etaMinutes: Int,
    var status: ScheduledProofOfDeliverySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledProofOfDeliverySlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledProofOfDeliverySlotStatus): Boolean {
        when (status) {
            ScheduledProofOfDeliverySlotStatus.DRAFT -> {
                when (next) {
                    ScheduledProofOfDeliverySlotStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledProofOfDeliverySlotStatus.PENDING -> {
                when (next) {
                    ScheduledProofOfDeliverySlotStatus.SUSPENDED, ScheduledProofOfDeliverySlotStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledProofOfDeliverySlotStatus.ACTIVE -> {
                when (next) {
                    ScheduledProofOfDeliverySlotStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledProofOfDeliverySlotStatus.SUSPENDED -> {
                when (next) {
                    ScheduledProofOfDeliverySlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledProofOfDeliverySlotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled proof of delivery slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled proof of delivery slot"} $code"

    fun projectedVolume(): Double = round(stopCount.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ScheduledProofOfDeliverySlot {
        return ScheduledProofOfDeliverySlot(
            code = code,
            name = name,
            visible = visible,
            country = country,
            checksum = checksum,
            stopCount = stopCount,
            summary = summary,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled proof of delivery slot in status draft. */
fun newScheduledProofOfDeliverySlot(code: String, name: String): ScheduledProofOfDeliverySlot {
    return ScheduledProofOfDeliverySlot(
        code = code,
        name = name,
        visible = false,
        country = "",
        checksum = "",
        stopCount = 0,
        summary = "",
        etaMinutes = 0,
        status = ScheduledProofOfDeliverySlotStatus.DRAFT,
    )
}
