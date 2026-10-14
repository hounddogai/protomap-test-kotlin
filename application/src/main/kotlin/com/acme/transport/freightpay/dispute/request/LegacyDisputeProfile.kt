package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dispute profile is in its life cycle. */
enum class LegacyDisputeProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy dispute profile. Its code is unique within a tenant. */
data class LegacyDisputeProfile(
    var code: String,
    var label: String,
    var approved: Boolean,
    var channel: String,
    var phone: String,
    var cost: Double,
    var threshold: Double,
    var status: LegacyDisputeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDisputeProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDisputeProfileStatus): Boolean {
        when (status) {
            LegacyDisputeProfileStatus.OPEN -> {
                when (next) {
                    LegacyDisputeProfileStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyDisputeProfileStatus.IN_REVIEW -> return next == LegacyDisputeProfileStatus.RESOLVED || next == LegacyDisputeProfileStatus.REOPENED
            LegacyDisputeProfileStatus.RESOLVED -> return next == LegacyDisputeProfileStatus.REOPENED || next == LegacyDisputeProfileStatus.ARCHIVED
            LegacyDisputeProfileStatus.REOPENED -> {
                when (next) {
                    LegacyDisputeProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDisputeProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy dispute profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy dispute profile"} $code"

    fun adjustedValue(): Double = round((threshold + cost) * 100) / 100

    fun snapshot(): LegacyDisputeProfile {
        return LegacyDisputeProfile(
            code = code,
            label = label,
            approved = approved,
            channel = channel,
            phone = phone,
            cost = cost,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy dispute profile in status open. */
fun newLegacyDisputeProfile(code: String, label: String): LegacyDisputeProfile {
    return LegacyDisputeProfile(
        code = code,
        label = label,
        approved = false,
        channel = "",
        phone = "",
        cost = 0.0,
        threshold = 0.0,
        status = LegacyDisputeProfileStatus.OPEN,
    )
}
