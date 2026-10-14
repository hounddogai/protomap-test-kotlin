package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan record is in its life cycle. */
enum class ServicePlanRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One service plan record, with what the planners need to act on it. */
data class ServicePlanRecord(
    var code: String,
    var name: String,
    var limit: Int,
    var distanceKm: Double,
    var region: String,
    var stopCount: Int,
    var capacity: Int,
    var batchSize: Int,
    var pallets: Int,
    var segment: String,
    var status: ServicePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanRecordStatus): Boolean {
        when (status) {
            ServicePlanRecordStatus.CREATED -> return next == ServicePlanRecordStatus.PUBLISHED || next == ServicePlanRecordStatus.RETIRED
            ServicePlanRecordStatus.VALIDATED -> return next == ServicePlanRecordStatus.PUBLISHED
            ServicePlanRecordStatus.PUBLISHED -> {
                when (next) {
                    ServicePlanRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("service plan record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((pallets.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanRecord {
        return ServicePlanRecord(
            code = code,
            name = name,
            limit = limit,
            distanceKm = distanceKm,
            region = region,
            stopCount = stopCount,
            capacity = capacity,
            batchSize = batchSize,
            pallets = pallets,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a service plan record in status created. */
fun newServicePlanRecord(code: String, name: String): ServicePlanRecord {
    return ServicePlanRecord(
        code = code,
        name = name,
        limit = 0,
        distanceKm = 0.0,
        region = "",
        stopCount = 0,
        capacity = 0,
        batchSize = 0,
        pallets = 0,
        segment = "",
        status = ServicePlanRecordStatus.CREATED,
    )
}
