package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient transit time request records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a lenient transit time request is in its life cycle. */
enum class LenientTransitTimeRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lenient transit time request, with what the planners need to act on it. */
data class LenientTransitTimeRequest(
    var code: String,
    var title: String,
    var priority: Int,
    var category: String,
    var updatedAt: Int,
    var status: LenientTransitTimeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTransitTimeRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTransitTimeRequestStatus): Boolean {
        when (status) {
            LenientTransitTimeRequestStatus.NEW -> return next == LenientTransitTimeRequestStatus.QUEUED || next == LenientTransitTimeRequestStatus.RUNNING
            LenientTransitTimeRequestStatus.QUEUED -> return next == LenientTransitTimeRequestStatus.RUNNING
            LenientTransitTimeRequestStatus.RUNNING -> {
                when (next) {
                    LenientTransitTimeRequestStatus.FAILED -> return true
                }
                return false
            }
            LenientTransitTimeRequestStatus.DONE -> return next == LenientTransitTimeRequestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientTransitTimeRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient transit time request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((priority.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientTransitTimeRequest {
        return LenientTransitTimeRequest(
            code = code,
            title = title,
            priority = priority,
            category = category,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient transit time request in status new. */
fun newLenientTransitTimeRequest(code: String, title: String): LenientTransitTimeRequest {
    return LenientTransitTimeRequest(
        code = code,
        title = title,
        priority = 0,
        category = "",
        updatedAt = 0,
        status = LenientTransitTimeRequestStatus.NEW,
    )
}
