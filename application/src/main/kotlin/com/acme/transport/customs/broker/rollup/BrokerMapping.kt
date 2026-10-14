package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker mapping is in its life cycle. */
enum class BrokerMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A broker mapping. Its code is unique within a tenant. */
data class BrokerMapping(
    var code: String,
    var title: String,
    var score: Double,
    var etaMinutes: Int,
    var durationMinutes: Int,
    var status: BrokerMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerMappingStatus): Boolean {
        when (status) {
            BrokerMappingStatus.IDLE -> {
                when (next) {
                    BrokerMappingStatus.READY, BrokerMappingStatus.STOPPED -> return true
                }
                return false
            }
            BrokerMappingStatus.WARMING -> {
                when (next) {
                    BrokerMappingStatus.READY, BrokerMappingStatus.DRAINING -> return true
                }
                return false
            }
            BrokerMappingStatus.READY -> return next == BrokerMappingStatus.DRAINING || next == BrokerMappingStatus.STOPPED
            BrokerMappingStatus.DRAINING -> return next == BrokerMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BrokerMappingStatus) {
        if (!canMoveTo(next)) {
            error("broker mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round((score - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): BrokerMapping {
        return BrokerMapping(
            code = code,
            title = title,
            score = score,
            etaMinutes = etaMinutes,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker mapping in status idle. */
fun newBrokerMapping(code: String, title: String): BrokerMapping {
    return BrokerMapping(
        code = code,
        title = title,
        score = 0.0,
        etaMinutes = 0,
        durationMinutes = 0,
        status = BrokerMappingStatus.IDLE,
    )
}
