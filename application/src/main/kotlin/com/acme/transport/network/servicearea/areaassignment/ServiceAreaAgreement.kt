package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area agreement is in its life cycle. */
enum class ServiceAreaAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service area agreement. Its code is unique within a tenant. */
data class ServiceAreaAgreement(
    var code: String,
    var title: String,
    var windowDays: Int,
    var ownerId: String,
    var etaMinutes: Int,
    var status: ServiceAreaAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaAgreementStatus): Boolean {
        when (status) {
            ServiceAreaAgreementStatus.OPEN -> return next == ServiceAreaAgreementStatus.IN_REVIEW
            ServiceAreaAgreementStatus.IN_REVIEW -> {
                when (next) {
                    ServiceAreaAgreementStatus.RESOLVED, ServiceAreaAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaAgreementStatus.RESOLVED -> {
                when (next) {
                    ServiceAreaAgreementStatus.REOPENED, ServiceAreaAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaAgreementStatus.REOPENED -> return next == ServiceAreaAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ServiceAreaAgreementStatus) {
        if (!canMoveTo(next)) {
            error("service area agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaAgreement {
        return ServiceAreaAgreement(
            code = code,
            title = title,
            windowDays = windowDays,
            ownerId = ownerId,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service area agreement in status open. */
fun newServiceAreaAgreement(code: String, title: String): ServiceAreaAgreement {
    return ServiceAreaAgreement(
        code = code,
        title = title,
        windowDays = 0,
        ownerId = "",
        etaMinutes = 0,
        status = ServiceAreaAgreementStatus.OPEN,
    )
}
