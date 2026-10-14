package com.acme.transport.network.servicearea.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area ledger is in its life cycle. */
enum class ServiceAreaLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One service area ledger, with what the planners need to act on it. */
data class ServiceAreaLedger(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var odometer: Int,
    var shiftCode: String,
    var status: ServiceAreaLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaLedgerStatus): Boolean {
        when (status) {
            ServiceAreaLedgerStatus.CREATED -> {
                when (next) {
                    ServiceAreaLedgerStatus.VALIDATED, ServiceAreaLedgerStatus.RETIRED -> return true
                }
                return false
            }
            ServiceAreaLedgerStatus.VALIDATED -> return next == ServiceAreaLedgerStatus.PUBLISHED || next == ServiceAreaLedgerStatus.RETIRED
            ServiceAreaLedgerStatus.PUBLISHED -> return next == ServiceAreaLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ServiceAreaLedgerStatus) {
        if (!canMoveTo(next)) {
            error("service area ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area ledger"} $code"

    fun effectiveExposure(): Double = round(odometer.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ServiceAreaLedger {
        return ServiceAreaLedger(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            odometer = odometer,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service area ledger in status created. */
fun newServiceAreaLedger(code: String, title: String): ServiceAreaLedger {
    return ServiceAreaLedger(
        code = code,
        title = title,
        etaMinutes = 0,
        odometer = 0,
        shiftCode = "",
        status = ServiceAreaLedgerStatus.CREATED,
    )
}
