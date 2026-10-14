package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default broker digest is in its life cycle. */
enum class DefaultBrokerDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default broker digest, with what the planners need to act on it. */
data class DefaultBrokerDigest(
    var code: String,
    var title: String,
    var floorValue: Double,
    var routeCode: String,
    var lane: String,
    var stopCount: Int,
    var status: DefaultBrokerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBrokerDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBrokerDigestStatus): Boolean {
        when (status) {
            DefaultBrokerDigestStatus.NEW -> {
                when (next) {
                    DefaultBrokerDigestStatus.DONE -> return true
                }
                return false
            }
            DefaultBrokerDigestStatus.QUEUED -> return next == DefaultBrokerDigestStatus.RUNNING || next == DefaultBrokerDigestStatus.FAILED
            DefaultBrokerDigestStatus.RUNNING -> {
                when (next) {
                    DefaultBrokerDigestStatus.DONE -> return true
                }
                return false
            }
            DefaultBrokerDigestStatus.DONE -> {
                when (next) {
                    DefaultBrokerDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultBrokerDigestStatus) {
        if (!canMoveTo(next)) {
            error("default broker digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round(stopCount.toDouble() * floorValue * 100) / 100

    fun snapshot(): DefaultBrokerDigest {
        return DefaultBrokerDigest(
            code = code,
            title = title,
            floorValue = floorValue,
            routeCode = routeCode,
            lane = lane,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default broker digest in status new. */
fun newDefaultBrokerDigest(code: String, title: String): DefaultBrokerDigest {
    return DefaultBrokerDigest(
        code = code,
        title = title,
        floorValue = 0.0,
        routeCode = "",
        lane = "",
        stopCount = 0,
        status = DefaultBrokerDigestStatus.NEW,
    )
}
