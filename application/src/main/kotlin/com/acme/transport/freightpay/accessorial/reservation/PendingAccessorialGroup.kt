package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending accessorial group is in its life cycle. */
enum class PendingAccessorialGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending accessorial group, with what the planners need to act on it. */
data class PendingAccessorialGroup(
    var code: String,
    var name: String,
    var batchSize: Int,
    var timezone: String,
    var cost: Double,
    var status: PendingAccessorialGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAccessorialGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAccessorialGroupStatus): Boolean {
        when (status) {
            PendingAccessorialGroupStatus.PLANNED -> {
                when (next) {
                    PendingAccessorialGroupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PendingAccessorialGroupStatus.SCHEDULED -> {
                when (next) {
                    PendingAccessorialGroupStatus.COMPLETED, PendingAccessorialGroupStatus.CANCELLED -> return true
                }
                return false
            }
            PendingAccessorialGroupStatus.IN_PROGRESS -> {
                when (next) {
                    PendingAccessorialGroupStatus.COMPLETED -> return true
                }
                return false
            }
            PendingAccessorialGroupStatus.COMPLETED -> return next == PendingAccessorialGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingAccessorialGroupStatus) {
        if (!canMoveTo(next)) {
            error("pending accessorial group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending accessorial group"} $code"

    fun weightedVolume(): Double = round((batchSize.toDouble() + cost) * 100) / 100

    fun snapshot(): PendingAccessorialGroup {
        return PendingAccessorialGroup(
            code = code,
            name = name,
            batchSize = batchSize,
            timezone = timezone,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending accessorial group in status planned. */
fun newPendingAccessorialGroup(code: String, name: String): PendingAccessorialGroup {
    return PendingAccessorialGroup(
        code = code,
        name = name,
        batchSize = 0,
        timezone = "",
        cost = 0.0,
        status = PendingAccessorialGroupStatus.PLANNED,
    )
}
