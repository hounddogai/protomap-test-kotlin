package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.normalize

/** Where a scheduled backhaul policy is in its life cycle. */
enum class ScheduledBackhaulPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled backhaul policy as the backhaul team keeps it. */
data class ScheduledBackhaulPolicy(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var limit: Int,
    var lane: String,
    var tags: MutableList<String>,
    var status: ScheduledBackhaulPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBackhaulPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBackhaulPolicyStatus): Boolean {
        when (status) {
            ScheduledBackhaulPolicyStatus.NEW -> return next == ScheduledBackhaulPolicyStatus.QUEUED || next == ScheduledBackhaulPolicyStatus.RUNNING
            ScheduledBackhaulPolicyStatus.QUEUED -> return next == ScheduledBackhaulPolicyStatus.RUNNING || next == ScheduledBackhaulPolicyStatus.DONE
            ScheduledBackhaulPolicyStatus.RUNNING -> return next == ScheduledBackhaulPolicyStatus.DONE
            ScheduledBackhaulPolicyStatus.DONE -> return next == ScheduledBackhaulPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledBackhaulPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scheduled backhaul policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): ScheduledBackhaulPolicy {
        return ScheduledBackhaulPolicy(
            code = code,
            title = title,
            pinned = pinned,
            limit = limit,
            lane = lane,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled backhaul policy in status new. */
fun newScheduledBackhaulPolicy(code: String, title: String): ScheduledBackhaulPolicy {
    return ScheduledBackhaulPolicy(
        code = code,
        title = title,
        pinned = false,
        limit = 0,
        lane = "",
        tags = mutableListOf<String>(),
        status = ScheduledBackhaulPolicyStatus.NEW,
    )
}
