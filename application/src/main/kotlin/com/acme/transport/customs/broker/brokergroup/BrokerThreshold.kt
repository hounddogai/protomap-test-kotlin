package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker threshold is in its life cycle. */
enum class BrokerThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a broker threshold and its current status. */
data class BrokerThreshold(
    var code: String,
    var title: String,
    var createdAt: Int,
    var phone: String,
    var loadKg: Double,
    var status: BrokerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerThresholdStatus): Boolean {
        when (status) {
            BrokerThresholdStatus.CREATED -> {
                when (next) {
                    BrokerThresholdStatus.VALIDATED, BrokerThresholdStatus.PUBLISHED -> return true
                }
                return false
            }
            BrokerThresholdStatus.VALIDATED -> {
                when (next) {
                    BrokerThresholdStatus.PUBLISHED -> return true
                }
                return false
            }
            BrokerThresholdStatus.PUBLISHED -> {
                when (next) {
                    BrokerThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("broker threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round(loadKg * createdAt.toDouble() * 100) / 100

    fun snapshot(): BrokerThreshold {
        return BrokerThreshold(
            code = code,
            title = title,
            createdAt = createdAt,
            phone = phone,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker threshold in status created. */
fun newBrokerThreshold(code: String, title: String): BrokerThreshold {
    return BrokerThreshold(
        code = code,
        title = title,
        createdAt = 0,
        phone = "",
        loadKg = 0.0,
        status = BrokerThresholdStatus.CREATED,
    )
}
