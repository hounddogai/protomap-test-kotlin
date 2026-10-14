package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered broker request is in its life cycle. */
enum class TieredBrokerRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered broker request and its current status. */
data class TieredBrokerRequest(
    var code: String,
    var label: String,
    var externalRef: String,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var ratio: Double,
    var quantity: Int,
    var unit: String,
    var summary: String,
    var segment: String,
    var status: TieredBrokerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBrokerRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBrokerRequestStatus): Boolean {
        when (status) {
            TieredBrokerRequestStatus.OPEN -> return next == TieredBrokerRequestStatus.RESOLVED || next == TieredBrokerRequestStatus.ARCHIVED
            TieredBrokerRequestStatus.IN_REVIEW -> {
                when (next) {
                    TieredBrokerRequestStatus.REOPENED, TieredBrokerRequestStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredBrokerRequestStatus.RESOLVED -> return next == TieredBrokerRequestStatus.ARCHIVED
            TieredBrokerRequestStatus.REOPENED -> return next == TieredBrokerRequestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredBrokerRequestStatus) {
        if (!canMoveTo(next)) {
            error("tiered broker request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered broker request"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round(ratio * updatedAt.toDouble() * 100) / 100

    fun snapshot(): TieredBrokerRequest {
        return TieredBrokerRequest(
            code = code,
            label = label,
            externalRef = externalRef,
            updatedAt = updatedAt,
            attributes = attributes,
            phone = phone,
            ratio = ratio,
            quantity = quantity,
            unit = unit,
            summary = summary,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered broker request in status open. */
fun newTieredBrokerRequest(code: String, label: String): TieredBrokerRequest {
    return TieredBrokerRequest(
        code = code,
        label = label,
        externalRef = "",
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        ratio = 0.0,
        quantity = 0,
        unit = "",
        summary = "",
        segment = "",
        status = TieredBrokerRequestStatus.OPEN,
    )
}
