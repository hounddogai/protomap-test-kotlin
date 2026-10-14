package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker exception records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker exception is in its life cycle. */
enum class BrokerExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A broker exception. Its code is unique within a tenant. */
data class BrokerException(
    var code: String,
    var name: String,
    var pallets: Int,
    var segment: String,
    var approved: Boolean,
    var threshold: Double,
    var ratio: Double,
    var priority: Int,
    var version: Int,
    var floorValue: Double,
    var status: BrokerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerExceptionStatus): Boolean {
        when (status) {
            BrokerExceptionStatus.CREATED -> {
                when (next) {
                    BrokerExceptionStatus.PUBLISHED -> return true
                }
                return false
            }
            BrokerExceptionStatus.VALIDATED -> return next == BrokerExceptionStatus.PUBLISHED || next == BrokerExceptionStatus.RETIRED
            BrokerExceptionStatus.PUBLISHED -> return next == BrokerExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("broker exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round(pallets.toDouble() * threshold * 100) / 100

    fun snapshot(): BrokerException {
        return BrokerException(
            code = code,
            name = name,
            pallets = pallets,
            segment = segment,
            approved = approved,
            threshold = threshold,
            ratio = ratio,
            priority = priority,
            version = version,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a broker exception in status created. */
fun newBrokerException(code: String, name: String): BrokerException {
    return BrokerException(
        code = code,
        name = name,
        pallets = 0,
        segment = "",
        approved = false,
        threshold = 0.0,
        ratio = 0.0,
        priority = 0,
        version = 0,
        floorValue = 0.0,
        status = BrokerExceptionStatus.CREATED,
    )
}
