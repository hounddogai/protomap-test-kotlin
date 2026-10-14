package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker bundle records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker bundle is in its life cycle. */
enum class BrokerBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A broker bundle. Its code is unique within a tenant. */
data class BrokerBundle(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var active: Boolean,
    var country: String,
    var fuelLiters: Double,
    var timezone: String,
    var createdAt: Int,
    var carrier: String,
    var label: String,
    var status: BrokerBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerBundleStatus): Boolean {
        when (status) {
            BrokerBundleStatus.OPEN -> {
                when (next) {
                    BrokerBundleStatus.IN_REVIEW -> return true
                }
                return false
            }
            BrokerBundleStatus.IN_REVIEW -> return next == BrokerBundleStatus.REOPENED || next == BrokerBundleStatus.ARCHIVED
            BrokerBundleStatus.RESOLVED -> return next == BrokerBundleStatus.REOPENED || next == BrokerBundleStatus.ARCHIVED
            BrokerBundleStatus.REOPENED -> return next == BrokerBundleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BrokerBundleStatus) {
        if (!canMoveTo(next)) {
            error("broker bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((createdAt.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): BrokerBundle {
        return BrokerBundle(
            code = code,
            name = name,
            tags = tags,
            active = active,
            country = country,
            fuelLiters = fuelLiters,
            timezone = timezone,
            createdAt = createdAt,
            carrier = carrier,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a broker bundle in status open. */
fun newBrokerBundle(code: String, name: String): BrokerBundle {
    return BrokerBundle(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        active = false,
        country = "",
        fuelLiters = 0.0,
        timezone = "",
        createdAt = 0,
        carrier = "",
        label = "",
        status = BrokerBundleStatus.OPEN,
    )
}
