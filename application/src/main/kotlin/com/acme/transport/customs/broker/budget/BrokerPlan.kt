package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker plan is in its life cycle. */
enum class BrokerPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a broker plan and its current status. */
data class BrokerPlan(
    var code: String,
    var title: String,
    var margin: Double,
    var batchSize: Int,
    var retries: Int,
    var ownerId: String,
    var pallets: Int,
    var externalRef: String,
    var status: BrokerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerPlanStatus): Boolean {
        when (status) {
            BrokerPlanStatus.PLANNED -> {
                when (next) {
                    BrokerPlanStatus.IN_PROGRESS, BrokerPlanStatus.COMPLETED -> return true
                }
                return false
            }
            BrokerPlanStatus.SCHEDULED -> {
                when (next) {
                    BrokerPlanStatus.COMPLETED, BrokerPlanStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerPlanStatus.IN_PROGRESS -> {
                when (next) {
                    BrokerPlanStatus.COMPLETED, BrokerPlanStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerPlanStatus.COMPLETED -> return next == BrokerPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BrokerPlanStatus) {
        if (!canMoveTo(next)) {
            error("broker plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"broker plan"} $code"

    fun netLoad(): Double = round((batchSize.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): BrokerPlan {
        return BrokerPlan(
            code = code,
            title = title,
            margin = margin,
            batchSize = batchSize,
            retries = retries,
            ownerId = ownerId,
            pallets = pallets,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker plan in status planned. */
fun newBrokerPlan(code: String, title: String): BrokerPlan {
    return BrokerPlan(
        code = code,
        title = title,
        margin = 0.0,
        batchSize = 0,
        retries = 0,
        ownerId = "",
        pallets = 0,
        externalRef = "",
        status = BrokerPlanStatus.PLANNED,
    )
}
