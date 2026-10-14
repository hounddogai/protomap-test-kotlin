package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped cross dock estimate records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a capped cross dock estimate is in its life cycle. */
enum class CappedCrossDockEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped cross dock estimate. Its code is unique within a tenant. */
data class CappedCrossDockEstimate(
    var code: String,
    var title: String,
    var createdAt: Int,
    var tags: MutableList<String>,
    var windowDays: Int,
    var name: String,
    var priority: Int,
    var visible: Boolean,
    var active: Boolean,
    var attempts: Int,
    var source: String,
    var status: CappedCrossDockEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCrossDockEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCrossDockEstimateStatus): Boolean {
        when (status) {
            CappedCrossDockEstimateStatus.OPEN -> {
                when (next) {
                    CappedCrossDockEstimateStatus.IN_REVIEW -> return true
                }
                return false
            }
            CappedCrossDockEstimateStatus.IN_REVIEW -> return next == CappedCrossDockEstimateStatus.RESOLVED || next == CappedCrossDockEstimateStatus.ARCHIVED
            CappedCrossDockEstimateStatus.RESOLVED -> return next == CappedCrossDockEstimateStatus.REOPENED
            CappedCrossDockEstimateStatus.REOPENED -> {
                when (next) {
                    CappedCrossDockEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCrossDockEstimateStatus) {
        if (!canMoveTo(next)) {
            error("capped cross dock estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped cross dock estimate"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((attempts.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): CappedCrossDockEstimate {
        return CappedCrossDockEstimate(
            code = code,
            title = title,
            createdAt = createdAt,
            tags = tags,
            windowDays = windowDays,
            name = name,
            priority = priority,
            visible = visible,
            active = active,
            attempts = attempts,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped cross dock estimate in status open. */
fun newCappedCrossDockEstimate(code: String, title: String): CappedCrossDockEstimate {
    return CappedCrossDockEstimate(
        code = code,
        title = title,
        createdAt = 0,
        tags = mutableListOf<String>(),
        windowDays = 0,
        name = "",
        priority = 0,
        visible = false,
        active = false,
        attempts = 0,
        source = "",
        status = CappedCrossDockEstimateStatus.OPEN,
    )
}
