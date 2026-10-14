package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute tier is in its life cycle. */
enum class DisputeTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dispute tier. Its code is unique within a tenant. */
data class DisputeTier(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var retries: Int,
    var channel: String,
    var locale: String,
    var summary: String,
    var priority: Int,
    var pallets: Int,
    var createdAt: Int,
    var status: DisputeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeTierStatus): Boolean {
        when (status) {
            DisputeTierStatus.PLANNED -> {
                when (next) {
                    DisputeTierStatus.SCHEDULED -> return true
                }
                return false
            }
            DisputeTierStatus.SCHEDULED -> {
                when (next) {
                    DisputeTierStatus.COMPLETED, DisputeTierStatus.CANCELLED -> return true
                }
                return false
            }
            DisputeTierStatus.IN_PROGRESS -> {
                when (next) {
                    DisputeTierStatus.CANCELLED -> return true
                }
                return false
            }
            DisputeTierStatus.COMPLETED -> return next == DisputeTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DisputeTierStatus) {
        if (!canMoveTo(next)) {
            error("dispute tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dispute tier"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((priority.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): DisputeTier {
        return DisputeTier(
            code = code,
            title = title,
            tags = tags,
            retries = retries,
            channel = channel,
            locale = locale,
            summary = summary,
            priority = priority,
            pallets = pallets,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dispute tier in status planned. */
fun newDisputeTier(code: String, title: String): DisputeTier {
    return DisputeTier(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        retries = 0,
        channel = "",
        locale = "",
        summary = "",
        priority = 0,
        pallets = 0,
        createdAt = 0,
        status = DisputeTierStatus.PLANNED,
    )
}
