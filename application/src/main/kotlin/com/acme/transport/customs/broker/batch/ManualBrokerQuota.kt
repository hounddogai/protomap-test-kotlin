package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual broker quota is in its life cycle. */
enum class ManualBrokerQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a manual broker quota and its current status. */
data class ManualBrokerQuota(
    var code: String,
    var name: String,
    var batchSize: Int,
    var approved: Boolean,
    var windowDays: Int,
    var priority: Int,
    var ownerId: String,
    var weight: Double,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var status: ManualBrokerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBrokerQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBrokerQuotaStatus): Boolean {
        when (status) {
            ManualBrokerQuotaStatus.PROPOSED -> {
                when (next) {
                    ManualBrokerQuotaStatus.APPROVED -> return true
                }
                return false
            }
            ManualBrokerQuotaStatus.APPROVED -> {
                when (next) {
                    ManualBrokerQuotaStatus.REJECTED -> return true
                }
                return false
            }
            ManualBrokerQuotaStatus.REJECTED -> return next == ManualBrokerQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualBrokerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual broker quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((priority.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ManualBrokerQuota {
        return ManualBrokerQuota(
            code = code,
            name = name,
            batchSize = batchSize,
            approved = approved,
            windowDays = windowDays,
            priority = priority,
            ownerId = ownerId,
            weight = weight,
            pinned = pinned,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual broker quota in status proposed. */
fun newManualBrokerQuota(code: String, name: String): ManualBrokerQuota {
    return ManualBrokerQuota(
        code = code,
        name = name,
        batchSize = 0,
        approved = false,
        windowDays = 0,
        priority = 0,
        ownerId = "",
        weight = 0.0,
        pinned = false,
        tags = mutableListOf<String>(),
        status = ManualBrokerQuotaStatus.PROPOSED,
    )
}
