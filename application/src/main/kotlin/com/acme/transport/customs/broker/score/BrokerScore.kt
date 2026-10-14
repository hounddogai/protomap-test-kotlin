package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker score records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker score is in its life cycle. */
enum class BrokerScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One broker score, with what the planners need to act on it. */
data class BrokerScore(
    var code: String,
    var title: String,
    var source: String,
    var unit: String,
    var active: Boolean,
    var threshold: Double,
    var notes: String,
    var pallets: Int,
    var currency: String,
    var status: BrokerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerScoreStatus): Boolean {
        when (status) {
            BrokerScoreStatus.DRAFT -> {
                when (next) {
                    BrokerScoreStatus.ACTIVE -> return true
                }
                return false
            }
            BrokerScoreStatus.PENDING -> return next == BrokerScoreStatus.ACTIVE || next == BrokerScoreStatus.CLOSED
            BrokerScoreStatus.ACTIVE -> {
                when (next) {
                    BrokerScoreStatus.CLOSED -> return true
                }
                return false
            }
            BrokerScoreStatus.SUSPENDED -> return next == BrokerScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BrokerScoreStatus) {
        if (!canMoveTo(next)) {
            error("broker score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((pallets.toDouble() - threshold) * 100) / 100

    fun snapshot(): BrokerScore {
        return BrokerScore(
            code = code,
            title = title,
            source = source,
            unit = unit,
            active = active,
            threshold = threshold,
            notes = notes,
            pallets = pallets,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a broker score in status draft. */
fun newBrokerScore(code: String, title: String): BrokerScore {
    return BrokerScore(
        code = code,
        title = title,
        source = "",
        unit = "",
        active = false,
        threshold = 0.0,
        notes = "",
        pallets = 0,
        currency = "",
        status = BrokerScoreStatus.DRAFT,
    )
}
