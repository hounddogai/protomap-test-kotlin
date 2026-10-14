package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive declaration milestone is in its life cycle. */
enum class AdaptiveDeclarationMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive declaration milestone. Its code is unique within a tenant. */
data class AdaptiveDeclarationMilestone(
    var code: String,
    var label: String,
    var lane: String,
    var priority: Int,
    var archived: Boolean,
    var discount: Double,
    var status: AdaptiveDeclarationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDeclarationMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDeclarationMilestoneStatus): Boolean {
        when (status) {
            AdaptiveDeclarationMilestoneStatus.OPEN -> {
                when (next) {
                    AdaptiveDeclarationMilestoneStatus.REOPENED, AdaptiveDeclarationMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveDeclarationMilestoneStatus.IN_REVIEW -> return next == AdaptiveDeclarationMilestoneStatus.REOPENED
            AdaptiveDeclarationMilestoneStatus.RESOLVED -> return next == AdaptiveDeclarationMilestoneStatus.REOPENED || next == AdaptiveDeclarationMilestoneStatus.ARCHIVED
            AdaptiveDeclarationMilestoneStatus.REOPENED -> {
                when (next) {
                    AdaptiveDeclarationMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDeclarationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive declaration milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round((discount - priority.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDeclarationMilestone {
        return AdaptiveDeclarationMilestone(
            code = code,
            label = label,
            lane = lane,
            priority = priority,
            archived = archived,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive declaration milestone in status open. */
fun newAdaptiveDeclarationMilestone(code: String, label: String): AdaptiveDeclarationMilestone {
    return AdaptiveDeclarationMilestone(
        code = code,
        label = label,
        lane = "",
        priority = 0,
        archived = false,
        discount = 0.0,
        status = AdaptiveDeclarationMilestoneStatus.OPEN,
    )
}
