package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break baseline records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break baseline is in its life cycle. */
enum class RestBreakBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rest break baseline. Its code is unique within a tenant. */
data class RestBreakBaseline(
    var code: String,
    var name: String,
    var ratio: Double,
    var depot: String,
    var visible: Boolean,
    var pinned: Boolean,
    var expiresAt: Int,
    var cost: Double,
    var title: String,
    var rate: Double,
    var status: RestBreakBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakBaselineStatus): Boolean {
        when (status) {
            RestBreakBaselineStatus.OPEN -> {
                when (next) {
                    RestBreakBaselineStatus.REOPENED -> return true
                }
                return false
            }
            RestBreakBaselineStatus.IN_REVIEW -> return next == RestBreakBaselineStatus.RESOLVED
            RestBreakBaselineStatus.RESOLVED -> return next == RestBreakBaselineStatus.REOPENED
            RestBreakBaselineStatus.REOPENED -> return next == RestBreakBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RestBreakBaselineStatus) {
        if (!canMoveTo(next)) {
            error("rest break baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((expiresAt.toDouble() + rate) * 100) / 100

    fun snapshot(): RestBreakBaseline {
        return RestBreakBaseline(
            code = code,
            name = name,
            ratio = ratio,
            depot = depot,
            visible = visible,
            pinned = pinned,
            expiresAt = expiresAt,
            cost = cost,
            title = title,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rest break baseline in status open. */
fun newRestBreakBaseline(code: String, name: String): RestBreakBaseline {
    return RestBreakBaseline(
        code = code,
        name = name,
        ratio = 0.0,
        depot = "",
        visible = false,
        pinned = false,
        expiresAt = 0,
        cost = 0.0,
        title = "",
        rate = 0.0,
        status = RestBreakBaselineStatus.OPEN,
    )
}
