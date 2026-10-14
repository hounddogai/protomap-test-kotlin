package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area snapshot is in its life cycle. */
enum class ServiceAreaSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One service area snapshot, with what the planners need to act on it. */
data class ServiceAreaSnapshot(
    var code: String,
    var title: String,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var region: String,
    var odometer: Int,
    var version: Int,
    var unit: String,
    var status: ServiceAreaSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaSnapshotStatus): Boolean {
        when (status) {
            ServiceAreaSnapshotStatus.PROPOSED -> {
                when (next) {
                    ServiceAreaSnapshotStatus.APPROVED -> return true
                }
                return false
            }
            ServiceAreaSnapshotStatus.APPROVED -> return next == ServiceAreaSnapshotStatus.REJECTED || next == ServiceAreaSnapshotStatus.WITHDRAWN
            ServiceAreaSnapshotStatus.REJECTED -> {
                when (next) {
                    ServiceAreaSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("service area snapshot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"service area snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((version.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaSnapshot {
        return ServiceAreaSnapshot(
            code = code,
            title = title,
            cost = cost,
            attributes = attributes,
            region = region,
            odometer = odometer,
            version = version,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a service area snapshot in status proposed. */
fun newServiceAreaSnapshot(code: String, title: String): ServiceAreaSnapshot {
    return ServiceAreaSnapshot(
        code = code,
        title = title,
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        region = "",
        odometer = 0,
        version = 0,
        unit = "",
        status = ServiceAreaSnapshotStatus.PROPOSED,
    )
}
