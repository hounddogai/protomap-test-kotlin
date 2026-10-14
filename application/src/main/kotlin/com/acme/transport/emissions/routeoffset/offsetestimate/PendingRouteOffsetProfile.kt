package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending route offset profile is in its life cycle. */
enum class PendingRouteOffsetProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending route offset profile, with what the planners need to act on it. */
data class PendingRouteOffsetProfile(
    var code: String,
    var title: String,
    var quantity: Int,
    var limit: Int,
    var ownerId: String,
    var status: PendingRouteOffsetProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRouteOffsetProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRouteOffsetProfileStatus): Boolean {
        when (status) {
            PendingRouteOffsetProfileStatus.NEW -> return next == PendingRouteOffsetProfileStatus.RUNNING || next == PendingRouteOffsetProfileStatus.DONE
            PendingRouteOffsetProfileStatus.QUEUED -> return next == PendingRouteOffsetProfileStatus.DONE
            PendingRouteOffsetProfileStatus.RUNNING -> return next == PendingRouteOffsetProfileStatus.DONE
            PendingRouteOffsetProfileStatus.DONE -> {
                when (next) {
                    PendingRouteOffsetProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRouteOffsetProfileStatus) {
        if (!canMoveTo(next)) {
            error("pending route offset profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((limit.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): PendingRouteOffsetProfile {
        return PendingRouteOffsetProfile(
            code = code,
            title = title,
            quantity = quantity,
            limit = limit,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending route offset profile in status new. */
fun newPendingRouteOffsetProfile(code: String, title: String): PendingRouteOffsetProfile {
    return PendingRouteOffsetProfile(
        code = code,
        title = title,
        quantity = 0,
        limit = 0,
        ownerId = "",
        status = PendingRouteOffsetProfileStatus.NEW,
    )
}
