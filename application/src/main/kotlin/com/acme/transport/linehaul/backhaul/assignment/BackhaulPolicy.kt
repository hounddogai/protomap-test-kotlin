package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul policy is in its life cycle. */
enum class BackhaulPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A backhaul policy. Its code is unique within a tenant. */
data class BackhaulPolicy(
    var code: String,
    var title: String,
    var priority: Int,
    var sequence: Int,
    var depot: String,
    var startsAt: Int,
    var unit: String,
    var segment: String,
    var reviewer: String,
    var limit: Int,
    var trailerId: String,
    var status: BackhaulPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulPolicyStatus): Boolean {
        when (status) {
            BackhaulPolicyStatus.OPEN -> return next == BackhaulPolicyStatus.IN_REVIEW || next == BackhaulPolicyStatus.ARCHIVED
            BackhaulPolicyStatus.IN_REVIEW -> {
                when (next) {
                    BackhaulPolicyStatus.RESOLVED -> return true
                }
                return false
            }
            BackhaulPolicyStatus.RESOLVED -> return next == BackhaulPolicyStatus.REOPENED || next == BackhaulPolicyStatus.ARCHIVED
            BackhaulPolicyStatus.REOPENED -> {
                when (next) {
                    BackhaulPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulPolicyStatus) {
        if (!canMoveTo(next)) {
            error("backhaul policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((limit.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): BackhaulPolicy {
        return BackhaulPolicy(
            code = code,
            title = title,
            priority = priority,
            sequence = sequence,
            depot = depot,
            startsAt = startsAt,
            unit = unit,
            segment = segment,
            reviewer = reviewer,
            limit = limit,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a backhaul policy in status open. */
fun newBackhaulPolicy(code: String, title: String): BackhaulPolicy {
    return BackhaulPolicy(
        code = code,
        title = title,
        priority = 0,
        sequence = 0,
        depot = "",
        startsAt = 0,
        unit = "",
        segment = "",
        reviewer = "",
        limit = 0,
        trailerId = "",
        status = BackhaulPolicyStatus.OPEN,
    )
}
