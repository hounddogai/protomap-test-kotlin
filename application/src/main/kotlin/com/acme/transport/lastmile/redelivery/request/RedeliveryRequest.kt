package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery request records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery request is in its life cycle. */
enum class RedeliveryRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A redelivery request as the redelivery team keeps it. */
data class RedeliveryRequest(
    var code: String,
    var name: String,
    var retries: Int,
    var category: String,
    var description: String,
    var locale: String,
    var title: String,
    var version: Int,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var status: RedeliveryRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryRequestStatus): Boolean {
        when (status) {
            RedeliveryRequestStatus.IDLE -> return next == RedeliveryRequestStatus.WARMING
            RedeliveryRequestStatus.WARMING -> return next == RedeliveryRequestStatus.READY || next == RedeliveryRequestStatus.DRAINING
            RedeliveryRequestStatus.READY -> return next == RedeliveryRequestStatus.DRAINING || next == RedeliveryRequestStatus.STOPPED
            RedeliveryRequestStatus.DRAINING -> {
                when (next) {
                    RedeliveryRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryRequestStatus) {
        if (!canMoveTo(next)) {
            error("redelivery request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((retries.toDouble() + loadKg) * 100) / 100

    fun snapshot(): RedeliveryRequest {
        return RedeliveryRequest(
            code = code,
            name = name,
            retries = retries,
            category = category,
            description = description,
            locale = locale,
            title = title,
            version = version,
            rate = rate,
            attributes = attributes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a redelivery request in status idle. */
fun newRedeliveryRequest(code: String, name: String): RedeliveryRequest {
    return RedeliveryRequest(
        code = code,
        name = name,
        retries = 0,
        category = "",
        description = "",
        locale = "",
        title = "",
        version = 0,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        status = RedeliveryRequestStatus.IDLE,
    )
}
