package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan ledger is in its life cycle. */
enum class ServicePlanLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service plan ledger as the service plan team keeps it. */
data class ServicePlanLedger(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var ceiling: Double,
    var driverId: String,
    var status: ServicePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanLedgerStatus): Boolean {
        when (status) {
            ServicePlanLedgerStatus.REQUESTED -> {
                when (next) {
                    ServicePlanLedgerStatus.REVOKED, ServicePlanLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanLedgerStatus.GRANTED -> {
                when (next) {
                    ServicePlanLedgerStatus.REVOKED, ServicePlanLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanLedgerStatus.REVOKED -> {
                when (next) {
                    ServicePlanLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("service plan ledger $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round(ceiling * updatedAt.toDouble() * 100) / 100

    fun snapshot(): ServicePlanLedger {
        return ServicePlanLedger(
            code = code,
            label = label,
            updatedAt = updatedAt,
            ceiling = ceiling,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service plan ledger in status requested. */
fun newServicePlanLedger(code: String, label: String): ServicePlanLedger {
    return ServicePlanLedger(
        code = code,
        label = label,
        updatedAt = 0,
        ceiling = 0.0,
        driverId = "",
        status = ServicePlanLedgerStatus.REQUESTED,
    )
}
