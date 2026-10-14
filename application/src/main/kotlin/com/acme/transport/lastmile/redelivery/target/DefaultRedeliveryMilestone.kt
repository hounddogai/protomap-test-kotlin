package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default redelivery milestone is in its life cycle. */
enum class DefaultRedeliveryMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default redelivery milestone and its current status. */
data class DefaultRedeliveryMilestone(
    var code: String,
    var label: String,
    var depot: String,
    var pallets: Int,
    var windowDays: Int,
    var visible: Boolean,
    var margin: Double,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var status: DefaultRedeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRedeliveryMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRedeliveryMilestoneStatus): Boolean {
        when (status) {
            DefaultRedeliveryMilestoneStatus.PLANNED -> {
                when (next) {
                    DefaultRedeliveryMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultRedeliveryMilestoneStatus.SCHEDULED -> {
                when (next) {
                    DefaultRedeliveryMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultRedeliveryMilestoneStatus.IN_PROGRESS -> return next == DefaultRedeliveryMilestoneStatus.COMPLETED
            DefaultRedeliveryMilestoneStatus.COMPLETED -> return next == DefaultRedeliveryMilestoneStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultRedeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("default redelivery milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round(margin * pallets.toDouble() * 100) / 100

    fun snapshot(): DefaultRedeliveryMilestone {
        return DefaultRedeliveryMilestone(
            code = code,
            label = label,
            depot = depot,
            pallets = pallets,
            windowDays = windowDays,
            visible = visible,
            margin = margin,
            updatedAt = updatedAt,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default redelivery milestone in status planned. */
fun newDefaultRedeliveryMilestone(code: String, label: String): DefaultRedeliveryMilestone {
    return DefaultRedeliveryMilestone(
        code = code,
        label = label,
        depot = "",
        pallets = 0,
        windowDays = 0,
        visible = false,
        margin = 0.0,
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        status = DefaultRedeliveryMilestoneStatus.PLANNED,
    )
}
