package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered broker policy is in its life cycle. */
enum class TieredBrokerPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered broker policy. Its code is unique within a tenant. */
data class TieredBrokerPolicy(
    var code: String,
    var title: String,
    var limit: Int,
    var trailerId: String,
    var budget: Double,
    var checksum: String,
    var status: TieredBrokerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBrokerPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBrokerPolicyStatus): Boolean {
        when (status) {
            TieredBrokerPolicyStatus.NEW -> return next == TieredBrokerPolicyStatus.QUEUED
            TieredBrokerPolicyStatus.QUEUED -> return next == TieredBrokerPolicyStatus.RUNNING || next == TieredBrokerPolicyStatus.DONE
            TieredBrokerPolicyStatus.RUNNING -> {
                when (next) {
                    TieredBrokerPolicyStatus.DONE, TieredBrokerPolicyStatus.FAILED -> return true
                }
                return false
            }
            TieredBrokerPolicyStatus.DONE -> {
                when (next) {
                    TieredBrokerPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredBrokerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("tiered broker policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((limit.toDouble() - budget) * 100) / 100

    fun snapshot(): TieredBrokerPolicy {
        return TieredBrokerPolicy(
            code = code,
            title = title,
            limit = limit,
            trailerId = trailerId,
            budget = budget,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered broker policy in status new. */
fun newTieredBrokerPolicy(code: String, title: String): TieredBrokerPolicy {
    return TieredBrokerPolicy(
        code = code,
        title = title,
        limit = 0,
        trailerId = "",
        budget = 0.0,
        checksum = "",
        status = TieredBrokerPolicyStatus.NEW,
    )
}
