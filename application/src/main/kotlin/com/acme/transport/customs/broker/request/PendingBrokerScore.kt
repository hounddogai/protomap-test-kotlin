package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending broker score is in its life cycle. */
enum class PendingBrokerScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending broker score and its current status. */
data class PendingBrokerScore(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var externalRef: String,
    var cost: Double,
    var attempts: Int,
    var threshold: Double,
    var weight: Double,
    var category: String,
    var status: PendingBrokerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBrokerScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBrokerScoreStatus): Boolean {
        when (status) {
            PendingBrokerScoreStatus.CREATED -> return next == PendingBrokerScoreStatus.VALIDATED || next == PendingBrokerScoreStatus.PUBLISHED
            PendingBrokerScoreStatus.VALIDATED -> {
                when (next) {
                    PendingBrokerScoreStatus.PUBLISHED, PendingBrokerScoreStatus.RETIRED -> return true
                }
                return false
            }
            PendingBrokerScoreStatus.PUBLISHED -> return next == PendingBrokerScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingBrokerScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending broker score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending broker score"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((cost + attempts.toDouble()) * 100) / 100

    fun snapshot(): PendingBrokerScore {
        return PendingBrokerScore(
            code = code,
            name = name,
            attributes = attributes,
            externalRef = externalRef,
            cost = cost,
            attempts = attempts,
            threshold = threshold,
            weight = weight,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending broker score in status created. */
fun newPendingBrokerScore(code: String, name: String): PendingBrokerScore {
    return PendingBrokerScore(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        externalRef = "",
        cost = 0.0,
        attempts = 0,
        threshold = 0.0,
        weight = 0.0,
        category = "",
        status = PendingBrokerScoreStatus.CREATED,
    )
}
