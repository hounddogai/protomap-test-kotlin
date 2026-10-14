package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial allocation is in its life cycle. */
enum class AccessorialAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One accessorial allocation, with what the planners need to act on it. */
data class AccessorialAllocation(
    var code: String,
    var title: String,
    var quantity: Int,
    var locale: String,
    var driverId: String,
    var reviewer: String,
    var label: String,
    var country: String,
    var version: Int,
    var status: AccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialAllocationStatus): Boolean {
        when (status) {
            AccessorialAllocationStatus.CREATED -> return next == AccessorialAllocationStatus.VALIDATED || next == AccessorialAllocationStatus.PUBLISHED
            AccessorialAllocationStatus.VALIDATED -> return next == AccessorialAllocationStatus.PUBLISHED
            AccessorialAllocationStatus.PUBLISHED -> {
                when (next) {
                    AccessorialAllocationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("accessorial allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"accessorial allocation"} $code"

    fun effectiveLoad(): Double = round((quantity.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): AccessorialAllocation {
        return AccessorialAllocation(
            code = code,
            title = title,
            quantity = quantity,
            locale = locale,
            driverId = driverId,
            reviewer = reviewer,
            label = label,
            country = country,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a accessorial allocation in status created. */
fun newAccessorialAllocation(code: String, title: String): AccessorialAllocation {
    return AccessorialAllocation(
        code = code,
        title = title,
        quantity = 0,
        locale = "",
        driverId = "",
        reviewer = "",
        label = "",
        country = "",
        version = 0,
        status = AccessorialAllocationStatus.CREATED,
    )
}
