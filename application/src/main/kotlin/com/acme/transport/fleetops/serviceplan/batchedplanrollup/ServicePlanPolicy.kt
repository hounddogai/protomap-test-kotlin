package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan policy is in its life cycle. */
enum class ServicePlanPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service plan policy as the service plan team keeps it. */
data class ServicePlanPolicy(
    var code: String,
    var label: String,
    var ownerId: String,
    var title: String,
    var description: String,
    var updatedAt: Int,
    var lane: String,
    var floorValue: Double,
    var limit: Int,
    var status: ServicePlanPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanPolicyStatus): Boolean {
        when (status) {
            ServicePlanPolicyStatus.NEW -> {
                when (next) {
                    ServicePlanPolicyStatus.QUEUED, ServicePlanPolicyStatus.FAILED -> return true
                }
                return false
            }
            ServicePlanPolicyStatus.QUEUED -> return next == ServicePlanPolicyStatus.DONE
            ServicePlanPolicyStatus.RUNNING -> {
                when (next) {
                    ServicePlanPolicyStatus.FAILED -> return true
                }
                return false
            }
            ServicePlanPolicyStatus.DONE -> return next == ServicePlanPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServicePlanPolicyStatus) {
        if (!canMoveTo(next)) {
            error("service plan policy $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"service plan policy"} $code"

    fun adjustedValue(): Double = round(floorValue * limit.toDouble() * 100) / 100

    fun snapshot(): ServicePlanPolicy {
        return ServicePlanPolicy(
            code = code,
            label = label,
            ownerId = ownerId,
            title = title,
            description = description,
            updatedAt = updatedAt,
            lane = lane,
            floorValue = floorValue,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service plan policy in status new. */
fun newServicePlanPolicy(code: String, label: String): ServicePlanPolicy {
    return ServicePlanPolicy(
        code = code,
        label = label,
        ownerId = "",
        title = "",
        description = "",
        updatedAt = 0,
        lane = "",
        floorValue = 0.0,
        limit = 0,
        status = ServicePlanPolicyStatus.NEW,
    )
}
