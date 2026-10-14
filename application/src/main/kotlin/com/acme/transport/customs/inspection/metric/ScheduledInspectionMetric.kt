package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize

// The scheduled inspection metric records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a scheduled inspection metric is in its life cycle. */
enum class ScheduledInspectionMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled inspection metric, with what the planners need to act on it. */
data class ScheduledInspectionMetric(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var archived: Boolean,
    var pallets: Int,
    var contactEmail: String,
    var status: ScheduledInspectionMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledInspectionMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledInspectionMetricStatus): Boolean {
        when (status) {
            ScheduledInspectionMetricStatus.NEW -> {
                when (next) {
                    ScheduledInspectionMetricStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledInspectionMetricStatus.QUEUED -> {
                when (next) {
                    ScheduledInspectionMetricStatus.RUNNING, ScheduledInspectionMetricStatus.FAILED -> return true
                }
                return false
            }
            ScheduledInspectionMetricStatus.RUNNING -> {
                when (next) {
                    ScheduledInspectionMetricStatus.DONE -> return true
                }
                return false
            }
            ScheduledInspectionMetricStatus.DONE -> return next == ScheduledInspectionMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledInspectionMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled inspection metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): ScheduledInspectionMetric {
        return ScheduledInspectionMetric(
            code = code,
            name = name,
            tags = tags,
            archived = archived,
            pallets = pallets,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled inspection metric in status new. */
fun newScheduledInspectionMetric(code: String, name: String): ScheduledInspectionMetric {
    return ScheduledInspectionMetric(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        archived = false,
        pallets = 0,
        contactEmail = "",
        status = ScheduledInspectionMetricStatus.NEW,
    )
}
