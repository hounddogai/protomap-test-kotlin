package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan manifest is in its life cycle. */
enum class ServicePlanManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One service plan manifest, with what the planners need to act on it. */
data class ServicePlanManifest(
    var code: String,
    var name: String,
    var visible: Boolean,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var status: ServicePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanManifestStatus): Boolean {
        when (status) {
            ServicePlanManifestStatus.DRAFT -> {
                when (next) {
                    ServicePlanManifestStatus.PENDING, ServicePlanManifestStatus.ACTIVE -> return true
                }
                return false
            }
            ServicePlanManifestStatus.PENDING -> return next == ServicePlanManifestStatus.SUSPENDED || next == ServicePlanManifestStatus.CLOSED
            ServicePlanManifestStatus.ACTIVE -> return next == ServicePlanManifestStatus.SUSPENDED || next == ServicePlanManifestStatus.CLOSED
            ServicePlanManifestStatus.SUSPENDED -> return next == ServicePlanManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ServicePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("service plan manifest $code cannot move from $status to $next")
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

    fun adjustedTotal(): Double = round((durationMinutes.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanManifest {
        return ServicePlanManifest(
            code = code,
            name = name,
            visible = visible,
            pallets = pallets,
            attributes = attributes,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan manifest in status draft. */
fun newServicePlanManifest(code: String, name: String): ServicePlanManifest {
    return ServicePlanManifest(
        code = code,
        name = name,
        visible = false,
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        status = ServicePlanManifestStatus.DRAFT,
    )
}
