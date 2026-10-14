package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization agreement is in its life cycle. */
enum class UtilizationAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A utilization agreement. Its code is unique within a tenant. */
data class UtilizationAgreement(
    var code: String,
    var title: String,
    var loadKg: Double,
    var version: Int,
    var unit: String,
    var status: UtilizationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationAgreementStatus): Boolean {
        when (status) {
            UtilizationAgreementStatus.REQUESTED -> {
                when (next) {
                    UtilizationAgreementStatus.GRANTED, UtilizationAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            UtilizationAgreementStatus.GRANTED -> {
                when (next) {
                    UtilizationAgreementStatus.REVOKED, UtilizationAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            UtilizationAgreementStatus.REVOKED -> {
                when (next) {
                    UtilizationAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("utilization agreement $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((version.toDouble() - loadKg) * 100) / 100

    fun snapshot(): UtilizationAgreement {
        return UtilizationAgreement(
            code = code,
            title = title,
            loadKg = loadKg,
            version = version,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a utilization agreement in status requested. */
fun newUtilizationAgreement(code: String, title: String): UtilizationAgreement {
    return UtilizationAgreement(
        code = code,
        title = title,
        loadKg = 0.0,
        version = 0,
        unit = "",
        status = UtilizationAgreementStatus.REQUESTED,
    )
}
