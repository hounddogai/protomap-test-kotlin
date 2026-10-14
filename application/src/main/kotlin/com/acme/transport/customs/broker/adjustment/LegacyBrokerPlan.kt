package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy broker plan is in its life cycle. */
enum class LegacyBrokerPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy broker plan. Its code is unique within a tenant. */
data class LegacyBrokerPlan(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var distanceKm: Double,
    var priority: Int,
    var archived: Boolean,
    var title: String,
    var status: LegacyBrokerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBrokerPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBrokerPlanStatus): Boolean {
        when (status) {
            LegacyBrokerPlanStatus.DRAFT -> {
                when (next) {
                    LegacyBrokerPlanStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyBrokerPlanStatus.PENDING -> return next == LegacyBrokerPlanStatus.SUSPENDED || next == LegacyBrokerPlanStatus.CLOSED
            LegacyBrokerPlanStatus.ACTIVE -> return next == LegacyBrokerPlanStatus.SUSPENDED || next == LegacyBrokerPlanStatus.CLOSED
            LegacyBrokerPlanStatus.SUSPENDED -> {
                when (next) {
                    LegacyBrokerPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBrokerPlanStatus) {
        if (!canMoveTo(next)) {
            error("legacy broker plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((maximum + distanceKm) * 100) / 100

    fun snapshot(): LegacyBrokerPlan {
        return LegacyBrokerPlan(
            code = code,
            name = name,
            attributes = attributes,
            maximum = maximum,
            distanceKm = distanceKm,
            priority = priority,
            archived = archived,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy broker plan in status draft. */
fun newLegacyBrokerPlan(code: String, name: String): LegacyBrokerPlan {
    return LegacyBrokerPlan(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        distanceKm = 0.0,
        priority = 0,
        archived = false,
        title = "",
        status = LegacyBrokerPlanStatus.DRAFT,
    )
}
