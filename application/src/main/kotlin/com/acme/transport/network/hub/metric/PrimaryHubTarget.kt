package com.acme.transport.network.hub.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary hub target is in its life cycle. */
enum class PrimaryHubTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary hub target. Its code is unique within a tenant. */
data class PrimaryHubTarget(
    var code: String,
    var title: String,
    var version: Int,
    var country: String,
    var loadKg: Double,
    var sequence: Int,
    var ceiling: Double,
    var currency: String,
    var active: Boolean,
    var status: PrimaryHubTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryHubTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryHubTargetStatus): Boolean {
        when (status) {
            PrimaryHubTargetStatus.IDLE -> return next == PrimaryHubTargetStatus.WARMING || next == PrimaryHubTargetStatus.DRAINING
            PrimaryHubTargetStatus.WARMING -> {
                when (next) {
                    PrimaryHubTargetStatus.READY, PrimaryHubTargetStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryHubTargetStatus.READY -> return next == PrimaryHubTargetStatus.STOPPED
            PrimaryHubTargetStatus.DRAINING -> return next == PrimaryHubTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryHubTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary hub target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"primary hub target"} $code"

    fun weightedValue(): Double = round((version.toDouble() + loadKg) * 100) / 100

    fun snapshot(): PrimaryHubTarget {
        return PrimaryHubTarget(
            code = code,
            title = title,
            version = version,
            country = country,
            loadKg = loadKg,
            sequence = sequence,
            ceiling = ceiling,
            currency = currency,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary hub target in status idle. */
fun newPrimaryHubTarget(code: String, title: String): PrimaryHubTarget {
    return PrimaryHubTarget(
        code = code,
        title = title,
        version = 0,
        country = "",
        loadKg = 0.0,
        sequence = 0,
        ceiling = 0.0,
        currency = "",
        active = false,
        status = PrimaryHubTargetStatus.IDLE,
    )
}
