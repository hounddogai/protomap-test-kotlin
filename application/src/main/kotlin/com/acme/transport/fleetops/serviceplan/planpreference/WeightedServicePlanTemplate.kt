package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted service plan template is in its life cycle. */
enum class WeightedServicePlanTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted service plan template as the service plan team keeps it. */
data class WeightedServicePlanTemplate(
    var code: String,
    var title: String,
    var minimum: Double,
    var distanceKm: Double,
    var floorValue: Double,
    var description: String,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var capacity: Int,
    var name: String,
    var phone: String,
    var status: WeightedServicePlanTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServicePlanTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServicePlanTemplateStatus): Boolean {
        when (status) {
            WeightedServicePlanTemplateStatus.PROPOSED -> return next == WeightedServicePlanTemplateStatus.APPROVED || next == WeightedServicePlanTemplateStatus.WITHDRAWN
            WeightedServicePlanTemplateStatus.APPROVED -> return next == WeightedServicePlanTemplateStatus.REJECTED || next == WeightedServicePlanTemplateStatus.WITHDRAWN
            WeightedServicePlanTemplateStatus.REJECTED -> {
                when (next) {
                    WeightedServicePlanTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedServicePlanTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted service plan template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted service plan template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(minimum * floorValue * 100) / 100

    fun snapshot(): WeightedServicePlanTemplate {
        return WeightedServicePlanTemplate(
            code = code,
            title = title,
            minimum = minimum,
            distanceKm = distanceKm,
            floorValue = floorValue,
            description = description,
            attributes = attributes,
            pinned = pinned,
            capacity = capacity,
            name = name,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted service plan template in status proposed. */
fun newWeightedServicePlanTemplate(code: String, title: String): WeightedServicePlanTemplate {
    return WeightedServicePlanTemplate(
        code = code,
        title = title,
        minimum = 0.0,
        distanceKm = 0.0,
        floorValue = 0.0,
        description = "",
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        capacity = 0,
        name = "",
        phone = "",
        status = WeightedServicePlanTemplateStatus.PROPOSED,
    )
}
