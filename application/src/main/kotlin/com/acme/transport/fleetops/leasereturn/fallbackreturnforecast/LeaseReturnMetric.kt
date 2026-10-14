package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.text.normalize

/** Where a lease return metric is in its life cycle. */
enum class LeaseReturnMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lease return metric as the lease return team keeps it. */
data class LeaseReturnMetric(
    var code: String,
    var title: String,
    var region: String,
    var unit: String,
    var category: String,
    var externalRef: String,
    var tags: MutableList<String>,
    var priority: Int,
    var status: LeaseReturnMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnMetricStatus): Boolean {
        when (status) {
            LeaseReturnMetricStatus.NEW -> return next == LeaseReturnMetricStatus.RUNNING
            LeaseReturnMetricStatus.QUEUED -> return next == LeaseReturnMetricStatus.RUNNING
            LeaseReturnMetricStatus.RUNNING -> return next == LeaseReturnMetricStatus.DONE
            LeaseReturnMetricStatus.DONE -> {
                when (next) {
                    LeaseReturnMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnMetricStatus) {
        if (!canMoveTo(next)) {
            error("lease return metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): LeaseReturnMetric {
        return LeaseReturnMetric(
            code = code,
            title = title,
            region = region,
            unit = unit,
            category = category,
            externalRef = externalRef,
            tags = tags,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lease return metric in status new. */
fun newLeaseReturnMetric(code: String, title: String): LeaseReturnMetric {
    return LeaseReturnMetric(
        code = code,
        title = title,
        region = "",
        unit = "",
        category = "",
        externalRef = "",
        tags = mutableListOf<String>(),
        priority = 0,
        status = LeaseReturnMetricStatus.NEW,
    )
}
