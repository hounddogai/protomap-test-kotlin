package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan batch is in its life cycle. */
enum class ServicePlanBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One service plan batch, with what the planners need to act on it. */
data class ServicePlanBatch(
    var code: String,
    var label: String,
    var margin: Double,
    var retries: Int,
    var version: Int,
    var status: ServicePlanBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanBatchStatus): Boolean {
        when (status) {
            ServicePlanBatchStatus.REQUESTED -> return next == ServicePlanBatchStatus.REVOKED || next == ServicePlanBatchStatus.EXPIRED
            ServicePlanBatchStatus.GRANTED -> {
                when (next) {
                    ServicePlanBatchStatus.REVOKED, ServicePlanBatchStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanBatchStatus.REVOKED -> return next == ServicePlanBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanBatchStatus) {
        if (!canMoveTo(next)) {
            error("service plan batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((version.toDouble() + margin) * 100) / 100

    fun snapshot(): ServicePlanBatch {
        return ServicePlanBatch(
            code = code,
            label = label,
            margin = margin,
            retries = retries,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service plan batch in status requested. */
fun newServicePlanBatch(code: String, label: String): ServicePlanBatch {
    return ServicePlanBatch(
        code = code,
        label = label,
        margin = 0.0,
        retries = 0,
        version = 0,
        status = ServicePlanBatchStatus.REQUESTED,
    )
}
