package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default zone cycle is in its life cycle. */
enum class DefaultZoneCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default zone cycle. Its code is unique within a tenant. */
data class DefaultZoneCycle(
    var code: String,
    var title: String,
    var windowDays: Int,
    var expiresAt: Int,
    var checksum: String,
    var margin: Double,
    var lane: String,
    var budget: Double,
    var region: String,
    var ceiling: Double,
    var status: DefaultZoneCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultZoneCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultZoneCycleStatus): Boolean {
        when (status) {
            DefaultZoneCycleStatus.PROPOSED -> return next == DefaultZoneCycleStatus.WITHDRAWN
            DefaultZoneCycleStatus.APPROVED -> {
                when (next) {
                    DefaultZoneCycleStatus.REJECTED -> return true
                }
                return false
            }
            DefaultZoneCycleStatus.REJECTED -> return next == DefaultZoneCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultZoneCycleStatus) {
        if (!canMoveTo(next)) {
            error("default zone cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round((expiresAt.toDouble() - ceiling) * 100) / 100

    fun snapshot(): DefaultZoneCycle {
        return DefaultZoneCycle(
            code = code,
            title = title,
            windowDays = windowDays,
            expiresAt = expiresAt,
            checksum = checksum,
            margin = margin,
            lane = lane,
            budget = budget,
            region = region,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default zone cycle in status proposed. */
fun newDefaultZoneCycle(code: String, title: String): DefaultZoneCycle {
    return DefaultZoneCycle(
        code = code,
        title = title,
        windowDays = 0,
        expiresAt = 0,
        checksum = "",
        margin = 0.0,
        lane = "",
        budget = 0.0,
        region = "",
        ceiling = 0.0,
        status = DefaultZoneCycleStatus.PROPOSED,
    )
}
