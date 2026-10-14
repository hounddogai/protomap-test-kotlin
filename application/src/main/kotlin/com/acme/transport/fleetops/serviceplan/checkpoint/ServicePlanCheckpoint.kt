package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan checkpoint records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a service plan checkpoint is in its life cycle. */
enum class ServicePlanCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service plan checkpoint as the service plan team keeps it. */
data class ServicePlanCheckpoint(
    var code: String,
    var name: String,
    var driverId: String,
    var externalRef: String,
    var score: Double,
    var loadKg: Double,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var status: ServicePlanCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanCheckpointStatus): Boolean {
        when (status) {
            ServicePlanCheckpointStatus.PROPOSED -> return next == ServicePlanCheckpointStatus.APPROVED || next == ServicePlanCheckpointStatus.WITHDRAWN
            ServicePlanCheckpointStatus.APPROVED -> {
                when (next) {
                    ServicePlanCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            ServicePlanCheckpointStatus.REJECTED -> return next == ServicePlanCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ServicePlanCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("service plan checkpoint $code cannot move from $status to $next")
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

    fun weightedExposure(): Double = round((score + loadKg) * 100) / 100

    fun snapshot(): ServicePlanCheckpoint {
        return ServicePlanCheckpoint(
            code = code,
            name = name,
            driverId = driverId,
            externalRef = externalRef,
            score = score,
            loadKg = loadKg,
            channel = channel,
            attributes = attributes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a service plan checkpoint in status proposed. */
fun newServicePlanCheckpoint(code: String, name: String): ServicePlanCheckpoint {
    return ServicePlanCheckpoint(
        code = code,
        name = name,
        driverId = "",
        externalRef = "",
        score = 0.0,
        loadKg = 0.0,
        channel = "",
        attributes = mutableMapOf<String, String>(),
        dock = "",
        status = ServicePlanCheckpointStatus.PROPOSED,
    )
}
