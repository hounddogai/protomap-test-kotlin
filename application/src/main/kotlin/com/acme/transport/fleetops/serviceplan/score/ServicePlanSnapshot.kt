package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan snapshot is in its life cycle. */
enum class ServicePlanSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A service plan snapshot as the service plan team keeps it. */
data class ServicePlanSnapshot(
    var code: String,
    var label: String,
    var lane: String,
    var version: Int,
    var loadKg: Double,
    var archived: Boolean,
    var amount: Double,
    var approved: Boolean,
    var trailerId: String,
    var updatedAt: Int,
    var status: ServicePlanSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanSnapshotStatus): Boolean {
        when (status) {
            ServicePlanSnapshotStatus.DRAFT -> {
                when (next) {
                    ServicePlanSnapshotStatus.ACTIVE, ServicePlanSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            ServicePlanSnapshotStatus.PENDING -> {
                when (next) {
                    ServicePlanSnapshotStatus.ACTIVE, ServicePlanSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            ServicePlanSnapshotStatus.ACTIVE -> {
                when (next) {
                    ServicePlanSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            ServicePlanSnapshotStatus.SUSPENDED -> {
                when (next) {
                    ServicePlanSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("service plan snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((loadKg - amount) * 100) / 100

    fun snapshot(): ServicePlanSnapshot {
        return ServicePlanSnapshot(
            code = code,
            label = label,
            lane = lane,
            version = version,
            loadKg = loadKg,
            archived = archived,
            amount = amount,
            approved = approved,
            trailerId = trailerId,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service plan snapshot in status draft. */
fun newServicePlanSnapshot(code: String, label: String): ServicePlanSnapshot {
    return ServicePlanSnapshot(
        code = code,
        label = label,
        lane = "",
        version = 0,
        loadKg = 0.0,
        archived = false,
        amount = 0.0,
        approved = false,
        trailerId = "",
        updatedAt = 0,
        status = ServicePlanSnapshotStatus.DRAFT,
    )
}
