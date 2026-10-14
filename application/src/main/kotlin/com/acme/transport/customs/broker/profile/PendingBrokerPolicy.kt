package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending broker policy is in its life cycle. */
enum class PendingBrokerPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a pending broker policy and its current status. */
data class PendingBrokerPolicy(
    var code: String,
    var title: String,
    var limit: Int,
    var routeCode: String,
    var budget: Double,
    var status: PendingBrokerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBrokerPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBrokerPolicyStatus): Boolean {
        when (status) {
            PendingBrokerPolicyStatus.NEW -> {
                when (next) {
                    PendingBrokerPolicyStatus.FAILED -> return true
                }
                return false
            }
            PendingBrokerPolicyStatus.QUEUED -> return next == PendingBrokerPolicyStatus.DONE || next == PendingBrokerPolicyStatus.FAILED
            PendingBrokerPolicyStatus.RUNNING -> return next == PendingBrokerPolicyStatus.FAILED
            PendingBrokerPolicyStatus.DONE -> {
                when (next) {
                    PendingBrokerPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingBrokerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("pending broker policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending broker policy"} $code"

    fun weightedExposure(): Double = round(budget * limit.toDouble() * 100) / 100

    fun snapshot(): PendingBrokerPolicy {
        return PendingBrokerPolicy(
            code = code,
            title = title,
            limit = limit,
            routeCode = routeCode,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending broker policy in status new. */
fun newPendingBrokerPolicy(code: String, title: String): PendingBrokerPolicy {
    return PendingBrokerPolicy(
        code = code,
        title = title,
        limit = 0,
        routeCode = "",
        budget = 0.0,
        status = PendingBrokerPolicyStatus.NEW,
    )
}
