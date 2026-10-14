package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient cross dock quota is in its life cycle. */
enum class LenientCrossDockQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient cross dock quota, with what the planners need to act on it. */
data class LenientCrossDockQuota(
    var code: String,
    var name: String,
    var visible: Boolean,
    var pallets: Int,
    var floorValue: Double,
    var region: String,
    var timezone: String,
    var tags: MutableList<String>,
    var status: LenientCrossDockQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCrossDockQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCrossDockQuotaStatus): Boolean {
        when (status) {
            LenientCrossDockQuotaStatus.REQUESTED -> {
                when (next) {
                    LenientCrossDockQuotaStatus.GRANTED, LenientCrossDockQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            LenientCrossDockQuotaStatus.GRANTED -> return next == LenientCrossDockQuotaStatus.EXPIRED
            LenientCrossDockQuotaStatus.REVOKED -> return next == LenientCrossDockQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientCrossDockQuotaStatus) {
        if (!canMoveTo(next)) {
            error("lenient cross dock quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((floorValue + pallets.toDouble()) * 100) / 100

    fun snapshot(): LenientCrossDockQuota {
        return LenientCrossDockQuota(
            code = code,
            name = name,
            visible = visible,
            pallets = pallets,
            floorValue = floorValue,
            region = region,
            timezone = timezone,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient cross dock quota in status requested. */
fun newLenientCrossDockQuota(code: String, name: String): LenientCrossDockQuota {
    return LenientCrossDockQuota(
        code = code,
        name = name,
        visible = false,
        pallets = 0,
        floorValue = 0.0,
        region = "",
        timezone = "",
        tags = mutableListOf<String>(),
        status = LenientCrossDockQuotaStatus.REQUESTED,
    )
}
