package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker digest is in its life cycle. */
enum class BrokerDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One broker digest, with what the planners need to act on it. */
data class BrokerDigest(
    var code: String,
    var label: String,
    var stopCount: Int,
    var limit: Int,
    var durationMinutes: Int,
    var loadKg: Double,
    var pallets: Int,
    var budget: Double,
    var archived: Boolean,
    var status: BrokerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerDigestStatus): Boolean {
        when (status) {
            BrokerDigestStatus.REQUESTED -> return next == BrokerDigestStatus.EXPIRED
            BrokerDigestStatus.GRANTED -> {
                when (next) {
                    BrokerDigestStatus.REVOKED -> return true
                }
                return false
            }
            BrokerDigestStatus.REVOKED -> return next == BrokerDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BrokerDigestStatus) {
        if (!canMoveTo(next)) {
            error("broker digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((pallets.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): BrokerDigest {
        return BrokerDigest(
            code = code,
            label = label,
            stopCount = stopCount,
            limit = limit,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            pallets = pallets,
            budget = budget,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a broker digest in status requested. */
fun newBrokerDigest(code: String, label: String): BrokerDigest {
    return BrokerDigest(
        code = code,
        label = label,
        stopCount = 0,
        limit = 0,
        durationMinutes = 0,
        loadKg = 0.0,
        pallets = 0,
        budget = 0.0,
        archived = false,
        status = BrokerDigestStatus.REQUESTED,
    )
}
