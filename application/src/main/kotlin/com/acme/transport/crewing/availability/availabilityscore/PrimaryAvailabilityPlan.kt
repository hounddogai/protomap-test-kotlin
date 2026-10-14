package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary availability plan is in its life cycle. */
enum class PrimaryAvailabilityPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One primary availability plan, with what the planners need to act on it. */
data class PrimaryAvailabilityPlan(
    var code: String,
    var name: String,
    var ownerId: String,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var discount: Double,
    var routeCode: String,
    var score: Double,
    var approved: Boolean,
    var margin: Double,
    var status: PrimaryAvailabilityPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAvailabilityPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAvailabilityPlanStatus): Boolean {
        when (status) {
            PrimaryAvailabilityPlanStatus.PLANNED -> return next == PrimaryAvailabilityPlanStatus.SCHEDULED || next == PrimaryAvailabilityPlanStatus.COMPLETED
            PrimaryAvailabilityPlanStatus.SCHEDULED -> return next == PrimaryAvailabilityPlanStatus.COMPLETED
            PrimaryAvailabilityPlanStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryAvailabilityPlanStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryAvailabilityPlanStatus.COMPLETED -> return next == PrimaryAvailabilityPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryAvailabilityPlanStatus) {
        if (!canMoveTo(next)) {
            error("primary availability plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(discount * margin * 100) / 100

    fun snapshot(): PrimaryAvailabilityPlan {
        return PrimaryAvailabilityPlan(
            code = code,
            name = name,
            ownerId = ownerId,
            dock = dock,
            attributes = attributes,
            discount = discount,
            routeCode = routeCode,
            score = score,
            approved = approved,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary availability plan in status planned. */
fun newPrimaryAvailabilityPlan(code: String, name: String): PrimaryAvailabilityPlan {
    return PrimaryAvailabilityPlan(
        code = code,
        name = name,
        ownerId = "",
        dock = "",
        attributes = mutableMapOf<String, String>(),
        discount = 0.0,
        routeCode = "",
        score = 0.0,
        approved = false,
        margin = 0.0,
        status = PrimaryAvailabilityPlanStatus.PLANNED,
    )
}
