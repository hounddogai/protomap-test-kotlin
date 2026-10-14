package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary drop estimate is in its life cycle. */
enum class PrimaryDropEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary drop estimate, with what the planners need to act on it. */
data class PrimaryDropEstimate(
    var code: String,
    var title: String,
    var locale: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var version: Int,
    var status: PrimaryDropEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDropEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDropEstimateStatus): Boolean {
        when (status) {
            PrimaryDropEstimateStatus.IDLE -> return next == PrimaryDropEstimateStatus.READY || next == PrimaryDropEstimateStatus.STOPPED
            PrimaryDropEstimateStatus.WARMING -> {
                when (next) {
                    PrimaryDropEstimateStatus.DRAINING, PrimaryDropEstimateStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryDropEstimateStatus.READY -> return next == PrimaryDropEstimateStatus.DRAINING || next == PrimaryDropEstimateStatus.STOPPED
            PrimaryDropEstimateStatus.DRAINING -> {
                when (next) {
                    PrimaryDropEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDropEstimateStatus) {
        if (!canMoveTo(next)) {
            error("primary drop estimate $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"primary drop estimate"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(version.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): PrimaryDropEstimate {
        return PrimaryDropEstimate(
            code = code,
            title = title,
            locale = locale,
            pallets = pallets,
            tags = tags,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary drop estimate in status idle. */
fun newPrimaryDropEstimate(code: String, title: String): PrimaryDropEstimate {
    return PrimaryDropEstimate(
        code = code,
        title = title,
        locale = "",
        pallets = 0,
        tags = mutableListOf<String>(),
        version = 0,
        status = PrimaryDropEstimateStatus.IDLE,
    )
}
