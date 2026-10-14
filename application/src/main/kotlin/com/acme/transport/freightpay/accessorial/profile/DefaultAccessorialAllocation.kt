package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default accessorial allocation is in its life cycle. */
enum class DefaultAccessorialAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default accessorial allocation as the accessorial team keeps it. */
data class DefaultAccessorialAllocation(
    var code: String,
    var label: String,
    var region: String,
    var timezone: String,
    var limit: Int,
    var country: String,
    var phone: String,
    var weight: Double,
    var status: DefaultAccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAccessorialAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAccessorialAllocationStatus): Boolean {
        when (status) {
            DefaultAccessorialAllocationStatus.PROPOSED -> {
                when (next) {
                    DefaultAccessorialAllocationStatus.APPROVED, DefaultAccessorialAllocationStatus.REJECTED -> return true
                }
                return false
            }
            DefaultAccessorialAllocationStatus.APPROVED -> return next == DefaultAccessorialAllocationStatus.REJECTED || next == DefaultAccessorialAllocationStatus.WITHDRAWN
            DefaultAccessorialAllocationStatus.REJECTED -> return next == DefaultAccessorialAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultAccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default accessorial allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((weight + limit.toDouble()) * 100) / 100

    fun snapshot(): DefaultAccessorialAllocation {
        return DefaultAccessorialAllocation(
            code = code,
            label = label,
            region = region,
            timezone = timezone,
            limit = limit,
            country = country,
            phone = phone,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default accessorial allocation in status proposed. */
fun newDefaultAccessorialAllocation(code: String, label: String): DefaultAccessorialAllocation {
    return DefaultAccessorialAllocation(
        code = code,
        label = label,
        region = "",
        timezone = "",
        limit = 0,
        country = "",
        phone = "",
        weight = 0.0,
        status = DefaultAccessorialAllocationStatus.PROPOSED,
    )
}
