package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.normalize

/** Where a strict redelivery quota is in its life cycle. */
enum class StrictRedeliveryQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict redelivery quota, with what the planners need to act on it. */
data class StrictRedeliveryQuota(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var loadKg: Double,
    var active: Boolean,
    var status: StrictRedeliveryQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRedeliveryQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRedeliveryQuotaStatus): Boolean {
        when (status) {
            StrictRedeliveryQuotaStatus.IDLE -> {
                when (next) {
                    StrictRedeliveryQuotaStatus.DRAINING -> return true
                }
                return false
            }
            StrictRedeliveryQuotaStatus.WARMING -> return next == StrictRedeliveryQuotaStatus.READY || next == StrictRedeliveryQuotaStatus.DRAINING
            StrictRedeliveryQuotaStatus.READY -> return next == StrictRedeliveryQuotaStatus.STOPPED
            StrictRedeliveryQuotaStatus.DRAINING -> return next == StrictRedeliveryQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictRedeliveryQuotaStatus) {
        if (!canMoveTo(next)) {
            error("strict redelivery quota $code cannot move from $status to $next")
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

    fun measuredValue(): Double = loadKg

    fun snapshot(): StrictRedeliveryQuota {
        return StrictRedeliveryQuota(
            code = code,
            title = title,
            tags = tags,
            loadKg = loadKg,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict redelivery quota in status idle. */
fun newStrictRedeliveryQuota(code: String, title: String): StrictRedeliveryQuota {
    return StrictRedeliveryQuota(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        active = false,
        status = StrictRedeliveryQuotaStatus.IDLE,
    )
}
