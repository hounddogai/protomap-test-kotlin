package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered utilization digest is in its life cycle. */
enum class TieredUtilizationDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered utilization digest as the utilization team keeps it. */
data class TieredUtilizationDigest(
    var code: String,
    var name: String,
    var category: String,
    var ceiling: Double,
    var createdAt: Int,
    var externalRef: String,
    var country: String,
    var maximum: Double,
    var retries: Int,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var status: TieredUtilizationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredUtilizationDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredUtilizationDigestStatus): Boolean {
        when (status) {
            TieredUtilizationDigestStatus.NEW -> return next == TieredUtilizationDigestStatus.RUNNING
            TieredUtilizationDigestStatus.QUEUED -> return next == TieredUtilizationDigestStatus.RUNNING || next == TieredUtilizationDigestStatus.DONE
            TieredUtilizationDigestStatus.RUNNING -> return next == TieredUtilizationDigestStatus.DONE || next == TieredUtilizationDigestStatus.FAILED
            TieredUtilizationDigestStatus.DONE -> return next == TieredUtilizationDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredUtilizationDigestStatus) {
        if (!canMoveTo(next)) {
            error("tiered utilization digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((maximum - ceiling) * 100) / 100

    fun snapshot(): TieredUtilizationDigest {
        return TieredUtilizationDigest(
            code = code,
            name = name,
            category = category,
            ceiling = ceiling,
            createdAt = createdAt,
            externalRef = externalRef,
            country = country,
            maximum = maximum,
            retries = retries,
            attributes = attributes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered utilization digest in status new. */
fun newTieredUtilizationDigest(code: String, name: String): TieredUtilizationDigest {
    return TieredUtilizationDigest(
        code = code,
        name = name,
        category = "",
        ceiling = 0.0,
        createdAt = 0,
        externalRef = "",
        country = "",
        maximum = 0.0,
        retries = 0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        status = TieredUtilizationDigestStatus.NEW,
    )
}
