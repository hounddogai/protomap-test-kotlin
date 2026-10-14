package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global trailer yard milestone is in its life cycle. */
enum class GlobalTrailerYardMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global trailer yard milestone as the trailer yard team keeps it. */
data class GlobalTrailerYardMilestone(
    var code: String,
    var title: String,
    var weight: Double,
    var ratio: Double,
    var externalRef: String,
    var source: String,
    var retries: Int,
    var region: String,
    var etaMinutes: Int,
    var expiresAt: Int,
    var status: GlobalTrailerYardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerYardMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerYardMilestoneStatus): Boolean {
        when (status) {
            GlobalTrailerYardMilestoneStatus.REQUESTED -> return next == GlobalTrailerYardMilestoneStatus.EXPIRED
            GlobalTrailerYardMilestoneStatus.GRANTED -> return next == GlobalTrailerYardMilestoneStatus.REVOKED || next == GlobalTrailerYardMilestoneStatus.EXPIRED
            GlobalTrailerYardMilestoneStatus.REVOKED -> {
                when (next) {
                    GlobalTrailerYardMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTrailerYardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("global trailer yard milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((ratio - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): GlobalTrailerYardMilestone {
        return GlobalTrailerYardMilestone(
            code = code,
            title = title,
            weight = weight,
            ratio = ratio,
            externalRef = externalRef,
            source = source,
            retries = retries,
            region = region,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global trailer yard milestone in status requested. */
fun newGlobalTrailerYardMilestone(code: String, title: String): GlobalTrailerYardMilestone {
    return GlobalTrailerYardMilestone(
        code = code,
        title = title,
        weight = 0.0,
        ratio = 0.0,
        externalRef = "",
        source = "",
        retries = 0,
        region = "",
        etaMinutes = 0,
        expiresAt = 0,
        status = GlobalTrailerYardMilestoneStatus.REQUESTED,
    )
}
