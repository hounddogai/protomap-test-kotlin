package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy proof of delivery request is in its life cycle. */
enum class LegacyProofOfDeliveryRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One legacy proof of delivery request, with what the planners need to act on it. */
data class LegacyProofOfDeliveryRequest(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var timezone: String,
    var createdAt: Int,
    var updatedAt: Int,
    var visible: Boolean,
    var status: LegacyProofOfDeliveryRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyProofOfDeliveryRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyProofOfDeliveryRequestStatus): Boolean {
        when (status) {
            LegacyProofOfDeliveryRequestStatus.DRAFT -> {
                when (next) {
                    LegacyProofOfDeliveryRequestStatus.CLOSED -> return true
                }
                return false
            }
            LegacyProofOfDeliveryRequestStatus.PENDING -> return next == LegacyProofOfDeliveryRequestStatus.ACTIVE
            LegacyProofOfDeliveryRequestStatus.ACTIVE -> return next == LegacyProofOfDeliveryRequestStatus.SUSPENDED
            LegacyProofOfDeliveryRequestStatus.SUSPENDED -> return next == LegacyProofOfDeliveryRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyProofOfDeliveryRequestStatus) {
        if (!canMoveTo(next)) {
            error("legacy proof of delivery request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round(updatedAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): LegacyProofOfDeliveryRequest {
        return LegacyProofOfDeliveryRequest(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            timezone = timezone,
            createdAt = createdAt,
            updatedAt = updatedAt,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy proof of delivery request in status draft. */
fun newLegacyProofOfDeliveryRequest(code: String, title: String): LegacyProofOfDeliveryRequest {
    return LegacyProofOfDeliveryRequest(
        code = code,
        title = title,
        etaMinutes = 0,
        timezone = "",
        createdAt = 0,
        updatedAt = 0,
        visible = false,
        status = LegacyProofOfDeliveryRequestStatus.DRAFT,
    )
}
