package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.normalize

/** Where a global redelivery milestone is in its life cycle. */
enum class GlobalRedeliveryMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global redelivery milestone as the redelivery team keeps it. */
data class GlobalRedeliveryMilestone(
    var code: String,
    var name: String,
    var checksum: String,
    var title: String,
    var version: Int,
    var externalRef: String,
    var status: GlobalRedeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRedeliveryMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRedeliveryMilestoneStatus): Boolean {
        when (status) {
            GlobalRedeliveryMilestoneStatus.PLANNED -> {
                when (next) {
                    GlobalRedeliveryMilestoneStatus.SCHEDULED, GlobalRedeliveryMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalRedeliveryMilestoneStatus.SCHEDULED -> return next == GlobalRedeliveryMilestoneStatus.CANCELLED
            GlobalRedeliveryMilestoneStatus.IN_PROGRESS -> return next == GlobalRedeliveryMilestoneStatus.COMPLETED
            GlobalRedeliveryMilestoneStatus.COMPLETED -> {
                when (next) {
                    GlobalRedeliveryMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRedeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("global redelivery milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): GlobalRedeliveryMilestone {
        return GlobalRedeliveryMilestone(
            code = code,
            name = name,
            checksum = checksum,
            title = title,
            version = version,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global redelivery milestone in status planned. */
fun newGlobalRedeliveryMilestone(code: String, name: String): GlobalRedeliveryMilestone {
    return GlobalRedeliveryMilestone(
        code = code,
        name = name,
        checksum = "",
        title = "",
        version = 0,
        externalRef = "",
        status = GlobalRedeliveryMilestoneStatus.PLANNED,
    )
}
