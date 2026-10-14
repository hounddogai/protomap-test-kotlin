package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.normalize

/** Where a primary accessorial group is in its life cycle. */
enum class PrimaryAccessorialGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary accessorial group as the accessorial team keeps it. */
data class PrimaryAccessorialGroup(
    var code: String,
    var title: String,
    var batchSize: Int,
    var region: String,
    var locale: String,
    var status: PrimaryAccessorialGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAccessorialGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAccessorialGroupStatus): Boolean {
        when (status) {
            PrimaryAccessorialGroupStatus.PLANNED -> {
                when (next) {
                    PrimaryAccessorialGroupStatus.SCHEDULED, PrimaryAccessorialGroupStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryAccessorialGroupStatus.SCHEDULED -> {
                when (next) {
                    PrimaryAccessorialGroupStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryAccessorialGroupStatus.IN_PROGRESS -> return next == PrimaryAccessorialGroupStatus.COMPLETED || next == PrimaryAccessorialGroupStatus.CANCELLED
            PrimaryAccessorialGroupStatus.COMPLETED -> return next == PrimaryAccessorialGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryAccessorialGroupStatus) {
        if (!canMoveTo(next)) {
            error("primary accessorial group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary accessorial group"} $code"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): PrimaryAccessorialGroup {
        return PrimaryAccessorialGroup(
            code = code,
            title = title,
            batchSize = batchSize,
            region = region,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary accessorial group in status planned. */
fun newPrimaryAccessorialGroup(code: String, title: String): PrimaryAccessorialGroup {
    return PrimaryAccessorialGroup(
        code = code,
        title = title,
        batchSize = 0,
        region = "",
        locale = "",
        status = PrimaryAccessorialGroupStatus.PLANNED,
    )
}
