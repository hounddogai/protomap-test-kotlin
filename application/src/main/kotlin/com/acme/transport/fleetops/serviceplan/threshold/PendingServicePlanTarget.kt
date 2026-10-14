package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending service plan target is in its life cycle. */
enum class PendingServicePlanTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One pending service plan target, with what the planners need to act on it. */
data class PendingServicePlanTarget(
    var code: String,
    var name: String,
    var trailerId: String,
    var startsAt: Int,
    var category: String,
    var pallets: Int,
    var revision: Int,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var fuelLiters: Double,
    var status: PendingServicePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServicePlanTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServicePlanTargetStatus): Boolean {
        when (status) {
            PendingServicePlanTargetStatus.OPEN -> return next == PendingServicePlanTargetStatus.IN_REVIEW
            PendingServicePlanTargetStatus.IN_REVIEW -> return next == PendingServicePlanTargetStatus.RESOLVED || next == PendingServicePlanTargetStatus.REOPENED
            PendingServicePlanTargetStatus.RESOLVED -> return next == PendingServicePlanTargetStatus.ARCHIVED
            PendingServicePlanTargetStatus.REOPENED -> {
                when (next) {
                    PendingServicePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingServicePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("pending service plan target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((revision.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): PendingServicePlanTarget {
        return PendingServicePlanTarget(
            code = code,
            name = name,
            trailerId = trailerId,
            startsAt = startsAt,
            category = category,
            pallets = pallets,
            revision = revision,
            locale = locale,
            attributes = attributes,
            pinned = pinned,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending service plan target in status open. */
fun newPendingServicePlanTarget(code: String, name: String): PendingServicePlanTarget {
    return PendingServicePlanTarget(
        code = code,
        name = name,
        trailerId = "",
        startsAt = 0,
        category = "",
        pallets = 0,
        revision = 0,
        locale = "",
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        fuelLiters = 0.0,
        status = PendingServicePlanTargetStatus.OPEN,
    )
}
