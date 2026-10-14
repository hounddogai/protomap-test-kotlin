package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan adjustment is in its life cycle. */
enum class ServicePlanAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service plan adjustment. Its code is unique within a tenant. */
data class ServicePlanAdjustment(
    var code: String,
    var label: String,
    var loadKg: Double,
    var category: String,
    var routeCode: String,
    var maximum: Double,
    var segment: String,
    var quantity: Int,
    var pallets: Int,
    var status: ServicePlanAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanAdjustmentStatus): Boolean {
        when (status) {
            ServicePlanAdjustmentStatus.NEW -> {
                when (next) {
                    ServicePlanAdjustmentStatus.RUNNING, ServicePlanAdjustmentStatus.DONE -> return true
                }
                return false
            }
            ServicePlanAdjustmentStatus.QUEUED -> return next == ServicePlanAdjustmentStatus.RUNNING || next == ServicePlanAdjustmentStatus.DONE
            ServicePlanAdjustmentStatus.RUNNING -> return next == ServicePlanAdjustmentStatus.DONE || next == ServicePlanAdjustmentStatus.FAILED
            ServicePlanAdjustmentStatus.DONE -> return next == ServicePlanAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServicePlanAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("service plan adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round((pallets.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ServicePlanAdjustment {
        return ServicePlanAdjustment(
            code = code,
            label = label,
            loadKg = loadKg,
            category = category,
            routeCode = routeCode,
            maximum = maximum,
            segment = segment,
            quantity = quantity,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a service plan adjustment in status new. */
fun newServicePlanAdjustment(code: String, label: String): ServicePlanAdjustment {
    return ServicePlanAdjustment(
        code = code,
        label = label,
        loadKg = 0.0,
        category = "",
        routeCode = "",
        maximum = 0.0,
        segment = "",
        quantity = 0,
        pallets = 0,
        status = ServicePlanAdjustmentStatus.NEW,
    )
}
