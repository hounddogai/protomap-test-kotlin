package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan template records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan template is in its life cycle. */
enum class ServicePlanTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan template. Its code is unique within a tenant. */
data class ServicePlanTemplate(
    var code: String,
    var name: String,
    var amount: Double,
    var cost: Double,
    var capacity: Int,
    var threshold: Double,
    var dock: String,
    var phone: String,
    var ratio: Double,
    var pinned: Boolean,
    var budget: Double,
    var status: ServicePlanTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanTemplateStatus): Boolean {
        when (status) {
            ServicePlanTemplateStatus.CREATED -> {
                when (next) {
                    ServicePlanTemplateStatus.VALIDATED, ServicePlanTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            ServicePlanTemplateStatus.VALIDATED -> {
                when (next) {
                    ServicePlanTemplateStatus.PUBLISHED, ServicePlanTemplateStatus.RETIRED -> return true
                }
                return false
            }
            ServicePlanTemplateStatus.PUBLISHED -> return next == ServicePlanTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanTemplateStatus) {
        if (!canMoveTo(next)) {
            error("service plan template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((amount - budget) * 100) / 100

    fun snapshot(): ServicePlanTemplate {
        return ServicePlanTemplate(
            code = code,
            name = name,
            amount = amount,
            cost = cost,
            capacity = capacity,
            threshold = threshold,
            dock = dock,
            phone = phone,
            ratio = ratio,
            pinned = pinned,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan template in status created. */
fun newServicePlanTemplate(code: String, name: String): ServicePlanTemplate {
    return ServicePlanTemplate(
        code = code,
        name = name,
        amount = 0.0,
        cost = 0.0,
        capacity = 0,
        threshold = 0.0,
        dock = "",
        phone = "",
        ratio = 0.0,
        pinned = false,
        budget = 0.0,
        status = ServicePlanTemplateStatus.CREATED,
    )
}
