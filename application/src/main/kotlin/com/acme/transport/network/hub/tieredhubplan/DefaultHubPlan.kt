package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default hub plan records of network's hub work, and the rules, stores, and calculations around them.

/** Where a default hub plan is in its life cycle. */
enum class DefaultHubPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default hub plan as the hub team keeps it. */
data class DefaultHubPlan(
    var code: String,
    var label: String,
    var batchSize: Int,
    var archived: Boolean,
    var title: String,
    var revision: Int,
    var trailerId: String,
    var notes: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var budget: Double,
    var status: DefaultHubPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubPlanStatus): Boolean {
        when (status) {
            DefaultHubPlanStatus.OPEN -> return next == DefaultHubPlanStatus.ARCHIVED
            DefaultHubPlanStatus.IN_REVIEW -> return next == DefaultHubPlanStatus.RESOLVED || next == DefaultHubPlanStatus.REOPENED
            DefaultHubPlanStatus.RESOLVED -> {
                when (next) {
                    DefaultHubPlanStatus.REOPENED -> return true
                }
                return false
            }
            DefaultHubPlanStatus.REOPENED -> return next == DefaultHubPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultHubPlanStatus) {
        if (!canMoveTo(next)) {
            error("default hub plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round(batchSize.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): DefaultHubPlan {
        return DefaultHubPlan(
            code = code,
            label = label,
            batchSize = batchSize,
            archived = archived,
            title = title,
            revision = revision,
            trailerId = trailerId,
            notes = notes,
            tags = tags,
            updatedAt = updatedAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default hub plan in status open. */
fun newDefaultHubPlan(code: String, label: String): DefaultHubPlan {
    return DefaultHubPlan(
        code = code,
        label = label,
        batchSize = 0,
        archived = false,
        title = "",
        revision = 0,
        trailerId = "",
        notes = "",
        tags = mutableListOf<String>(),
        updatedAt = 0,
        budget = 0.0,
        status = DefaultHubPlanStatus.OPEN,
    )
}
