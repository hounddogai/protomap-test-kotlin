package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dispute profile is in its life cycle. */
enum class GlobalDisputeProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global dispute profile as the dispute team keeps it. */
data class GlobalDisputeProfile(
    var code: String,
    var title: String,
    var rate: Double,
    var active: Boolean,
    var externalRef: String,
    var region: String,
    var shiftCode: String,
    var locale: String,
    var minimum: Double,
    var limit: Int,
    var status: GlobalDisputeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDisputeProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDisputeProfileStatus): Boolean {
        when (status) {
            GlobalDisputeProfileStatus.REQUESTED -> {
                when (next) {
                    GlobalDisputeProfileStatus.REVOKED, GlobalDisputeProfileStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalDisputeProfileStatus.GRANTED -> return next == GlobalDisputeProfileStatus.REVOKED || next == GlobalDisputeProfileStatus.EXPIRED
            GlobalDisputeProfileStatus.REVOKED -> {
                when (next) {
                    GlobalDisputeProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDisputeProfileStatus) {
        if (!canMoveTo(next)) {
            error("global dispute profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((minimum - rate) * 100) / 100

    fun snapshot(): GlobalDisputeProfile {
        return GlobalDisputeProfile(
            code = code,
            title = title,
            rate = rate,
            active = active,
            externalRef = externalRef,
            region = region,
            shiftCode = shiftCode,
            locale = locale,
            minimum = minimum,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global dispute profile in status requested. */
fun newGlobalDisputeProfile(code: String, title: String): GlobalDisputeProfile {
    return GlobalDisputeProfile(
        code = code,
        title = title,
        rate = 0.0,
        active = false,
        externalRef = "",
        region = "",
        shiftCode = "",
        locale = "",
        minimum = 0.0,
        limit = 0,
        status = GlobalDisputeProfileStatus.REQUESTED,
    )
}
