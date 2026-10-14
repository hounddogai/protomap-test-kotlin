package com.acme.transport.customs.broker.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker cycle is in its life cycle. */
enum class BrokerCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A broker cycle. Its code is unique within a tenant. */
data class BrokerCycle(
    var code: String,
    var name: String,
    var score: Double,
    var margin: Double,
    var archived: Boolean,
    var status: BrokerCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerCycleStatus): Boolean {
        when (status) {
            BrokerCycleStatus.IDLE -> return next == BrokerCycleStatus.DRAINING || next == BrokerCycleStatus.STOPPED
            BrokerCycleStatus.WARMING -> {
                when (next) {
                    BrokerCycleStatus.DRAINING, BrokerCycleStatus.STOPPED -> return true
                }
                return false
            }
            BrokerCycleStatus.READY -> {
                when (next) {
                    BrokerCycleStatus.DRAINING, BrokerCycleStatus.STOPPED -> return true
                }
                return false
            }
            BrokerCycleStatus.DRAINING -> {
                when (next) {
                    BrokerCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerCycleStatus) {
        if (!canMoveTo(next)) {
            error("broker cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((score + margin) * 100) / 100

    fun snapshot(): BrokerCycle {
        return BrokerCycle(
            code = code,
            name = name,
            score = score,
            margin = margin,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a broker cycle in status idle. */
fun newBrokerCycle(code: String, name: String): BrokerCycle {
    return BrokerCycle(
        code = code,
        name = name,
        score = 0.0,
        margin = 0.0,
        archived = false,
        status = BrokerCycleStatus.IDLE,
    )
}
