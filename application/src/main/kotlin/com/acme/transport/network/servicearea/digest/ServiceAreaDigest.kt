package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.normalize

// The service area digest records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area digest is in its life cycle. */
enum class ServiceAreaDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A service area digest. Its code is unique within a tenant. */
data class ServiceAreaDigest(
    var code: String,
    var title: String,
    var locale: String,
    var visible: Boolean,
    var cost: Double,
    var status: ServiceAreaDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaDigestStatus): Boolean {
        when (status) {
            ServiceAreaDigestStatus.PLANNED -> return next == ServiceAreaDigestStatus.COMPLETED || next == ServiceAreaDigestStatus.CANCELLED
            ServiceAreaDigestStatus.SCHEDULED -> return next == ServiceAreaDigestStatus.CANCELLED
            ServiceAreaDigestStatus.IN_PROGRESS -> {
                when (next) {
                    ServiceAreaDigestStatus.COMPLETED, ServiceAreaDigestStatus.CANCELLED -> return true
                }
                return false
            }
            ServiceAreaDigestStatus.COMPLETED -> return next == ServiceAreaDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServiceAreaDigestStatus) {
        if (!canMoveTo(next)) {
            error("service area digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area digest"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): ServiceAreaDigest {
        return ServiceAreaDigest(
            code = code,
            title = title,
            locale = locale,
            visible = visible,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a service area digest in status planned. */
fun newServiceAreaDigest(code: String, title: String): ServiceAreaDigest {
    return ServiceAreaDigest(
        code = code,
        title = title,
        locale = "",
        visible = false,
        cost = 0.0,
        status = ServiceAreaDigestStatus.PLANNED,
    )
}
