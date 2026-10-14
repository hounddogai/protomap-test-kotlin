package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** Where a lenient proof of delivery profile is in its life cycle. */
enum class LenientProofOfDeliveryProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient proof of delivery profile, with what the planners need to act on it. */
data class LenientProofOfDeliveryProfile(
    var code: String,
    var title: String,
    var approved: Boolean,
    var ownerId: String,
    var region: String,
    var channel: String,
    var graceDays: Int,
    var status: LenientProofOfDeliveryProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientProofOfDeliveryProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientProofOfDeliveryProfileStatus): Boolean {
        when (status) {
            LenientProofOfDeliveryProfileStatus.IDLE -> return next == LenientProofOfDeliveryProfileStatus.DRAINING || next == LenientProofOfDeliveryProfileStatus.STOPPED
            LenientProofOfDeliveryProfileStatus.WARMING -> {
                when (next) {
                    LenientProofOfDeliveryProfileStatus.READY, LenientProofOfDeliveryProfileStatus.DRAINING -> return true
                }
                return false
            }
            LenientProofOfDeliveryProfileStatus.READY -> {
                when (next) {
                    LenientProofOfDeliveryProfileStatus.DRAINING, LenientProofOfDeliveryProfileStatus.STOPPED -> return true
                }
                return false
            }
            LenientProofOfDeliveryProfileStatus.DRAINING -> return next == LenientProofOfDeliveryProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientProofOfDeliveryProfileStatus) {
        if (!canMoveTo(next)) {
            error("lenient proof of delivery profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): LenientProofOfDeliveryProfile {
        return LenientProofOfDeliveryProfile(
            code = code,
            title = title,
            approved = approved,
            ownerId = ownerId,
            region = region,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient proof of delivery profile in status idle. */
fun newLenientProofOfDeliveryProfile(code: String, title: String): LenientProofOfDeliveryProfile {
    return LenientProofOfDeliveryProfile(
        code = code,
        title = title,
        approved = false,
        ownerId = "",
        region = "",
        channel = "",
        graceDays = 0,
        status = LenientProofOfDeliveryProfileStatus.IDLE,
    )
}
