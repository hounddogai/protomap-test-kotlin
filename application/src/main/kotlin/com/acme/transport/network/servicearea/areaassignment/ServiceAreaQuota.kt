package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area quota is in its life cycle. */
enum class ServiceAreaQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service area quota. Its code is unique within a tenant. */
data class ServiceAreaQuota(
    var code: String,
    var title: String,
    var category: String,
    var sequence: Int,
    var endsAt: Int,
    var budget: Double,
    var source: String,
    var status: ServiceAreaQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaQuotaStatus): Boolean {
        when (status) {
            ServiceAreaQuotaStatus.OPEN -> {
                when (next) {
                    ServiceAreaQuotaStatus.IN_REVIEW, ServiceAreaQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaQuotaStatus.IN_REVIEW -> {
                when (next) {
                    ServiceAreaQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaQuotaStatus.RESOLVED -> {
                when (next) {
                    ServiceAreaQuotaStatus.REOPENED, ServiceAreaQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaQuotaStatus.REOPENED -> {
                when (next) {
                    ServiceAreaQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaQuotaStatus) {
        if (!canMoveTo(next)) {
            error("service area quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"service area quota"} $code"

    fun netVolume(): Double = round((sequence.toDouble() - budget) * 100) / 100

    fun snapshot(): ServiceAreaQuota {
        return ServiceAreaQuota(
            code = code,
            title = title,
            category = category,
            sequence = sequence,
            endsAt = endsAt,
            budget = budget,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a service area quota in status open. */
fun newServiceAreaQuota(code: String, title: String): ServiceAreaQuota {
    return ServiceAreaQuota(
        code = code,
        title = title,
        category = "",
        sequence = 0,
        endsAt = 0,
        budget = 0.0,
        source = "",
        status = ServiceAreaQuotaStatus.OPEN,
    )
}
