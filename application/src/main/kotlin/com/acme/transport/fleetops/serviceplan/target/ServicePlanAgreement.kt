package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan agreement is in its life cycle. */
enum class ServicePlanAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan agreement. Its code is unique within a tenant. */
data class ServicePlanAgreement(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var unit: String,
    var depot: String,
    var endsAt: Int,
    var visible: Boolean,
    var dock: String,
    var sequence: Int,
    var limit: Int,
    var status: ServicePlanAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanAgreementStatus): Boolean {
        when (status) {
            ServicePlanAgreementStatus.CREATED -> {
                when (next) {
                    ServicePlanAgreementStatus.VALIDATED -> return true
                }
                return false
            }
            ServicePlanAgreementStatus.VALIDATED -> {
                when (next) {
                    ServicePlanAgreementStatus.PUBLISHED, ServicePlanAgreementStatus.RETIRED -> return true
                }
                return false
            }
            ServicePlanAgreementStatus.PUBLISHED -> return next == ServicePlanAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanAgreementStatus) {
        if (!canMoveTo(next)) {
            error("service plan agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"service plan agreement"} $code"

    fun projectedVolume(): Double = round((limit.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanAgreement {
        return ServicePlanAgreement(
            code = code,
            label = label,
            expiresAt = expiresAt,
            unit = unit,
            depot = depot,
            endsAt = endsAt,
            visible = visible,
            dock = dock,
            sequence = sequence,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a service plan agreement in status created. */
fun newServicePlanAgreement(code: String, label: String): ServicePlanAgreement {
    return ServicePlanAgreement(
        code = code,
        label = label,
        expiresAt = 0,
        unit = "",
        depot = "",
        endsAt = 0,
        visible = false,
        dock = "",
        sequence = 0,
        limit = 0,
        status = ServicePlanAgreementStatus.CREATED,
    )
}
