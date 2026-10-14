package com.acme.transport.network.hub.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hub baseline is in its life cycle. */
enum class ManualHubBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual hub baseline as the hub team keeps it. */
data class ManualHubBaseline(
    var code: String,
    var title: String,
    var sequence: Int,
    var reviewer: String,
    var priority: Int,
    var approved: Boolean,
    var quantity: Int,
    var driverId: String,
    var floorValue: Double,
    var capacity: Int,
    var rate: Double,
    var status: ManualHubBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHubBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHubBaselineStatus): Boolean {
        when (status) {
            ManualHubBaselineStatus.REQUESTED -> return next == ManualHubBaselineStatus.GRANTED || next == ManualHubBaselineStatus.EXPIRED
            ManualHubBaselineStatus.GRANTED -> {
                when (next) {
                    ManualHubBaselineStatus.REVOKED -> return true
                }
                return false
            }
            ManualHubBaselineStatus.REVOKED -> return next == ManualHubBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualHubBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual hub baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((rate + sequence.toDouble()) * 100) / 100

    fun snapshot(): ManualHubBaseline {
        return ManualHubBaseline(
            code = code,
            title = title,
            sequence = sequence,
            reviewer = reviewer,
            priority = priority,
            approved = approved,
            quantity = quantity,
            driverId = driverId,
            floorValue = floorValue,
            capacity = capacity,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual hub baseline in status requested. */
fun newManualHubBaseline(code: String, title: String): ManualHubBaseline {
    return ManualHubBaseline(
        code = code,
        title = title,
        sequence = 0,
        reviewer = "",
        priority = 0,
        approved = false,
        quantity = 0,
        driverId = "",
        floorValue = 0.0,
        capacity = 0,
        rate = 0.0,
        status = ManualHubBaselineStatus.REQUESTED,
    )
}
