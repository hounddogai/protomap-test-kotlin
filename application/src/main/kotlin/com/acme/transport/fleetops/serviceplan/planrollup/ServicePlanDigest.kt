package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan digest is in its life cycle. */
enum class ServicePlanDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One service plan digest, with what the planners need to act on it. */
data class ServicePlanDigest(
    var code: String,
    var name: String,
    var checksum: String,
    var ceiling: Double,
    var odometer: Int,
    var weight: Double,
    var status: ServicePlanDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanDigestStatus): Boolean {
        when (status) {
            ServicePlanDigestStatus.REQUESTED -> return next == ServicePlanDigestStatus.REVOKED
            ServicePlanDigestStatus.GRANTED -> {
                when (next) {
                    ServicePlanDigestStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanDigestStatus.REVOKED -> {
                when (next) {
                    ServicePlanDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanDigestStatus) {
        if (!canMoveTo(next)) {
            error("service plan digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(ceiling * weight * 100) / 100

    fun snapshot(): ServicePlanDigest {
        return ServicePlanDigest(
            code = code,
            name = name,
            checksum = checksum,
            ceiling = ceiling,
            odometer = odometer,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan digest in status requested. */
fun newServicePlanDigest(code: String, name: String): ServicePlanDigest {
    return ServicePlanDigest(
        code = code,
        name = name,
        checksum = "",
        ceiling = 0.0,
        odometer = 0,
        weight = 0.0,
        status = ServicePlanDigestStatus.REQUESTED,
    )
}
