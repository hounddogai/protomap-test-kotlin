package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge digest is in its life cycle. */
enum class TieredEvChargeDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered ev charge digest. Its code is unique within a tenant. */
data class TieredEvChargeDigest(
    var code: String,
    var name: String,
    var region: String,
    var channel: String,
    var maximum: Double,
    var lane: String,
    var budget: Double,
    var visible: Boolean,
    var windowDays: Int,
    var status: TieredEvChargeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeDigestStatus): Boolean {
        when (status) {
            TieredEvChargeDigestStatus.OPEN -> return next == TieredEvChargeDigestStatus.RESOLVED || next == TieredEvChargeDigestStatus.REOPENED
            TieredEvChargeDigestStatus.IN_REVIEW -> return next == TieredEvChargeDigestStatus.ARCHIVED
            TieredEvChargeDigestStatus.RESOLVED -> return next == TieredEvChargeDigestStatus.ARCHIVED
            TieredEvChargeDigestStatus.REOPENED -> return next == TieredEvChargeDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredEvChargeDigestStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((windowDays.toDouble() - maximum) * 100) / 100

    fun snapshot(): TieredEvChargeDigest {
        return TieredEvChargeDigest(
            code = code,
            name = name,
            region = region,
            channel = channel,
            maximum = maximum,
            lane = lane,
            budget = budget,
            visible = visible,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered ev charge digest in status open. */
fun newTieredEvChargeDigest(code: String, name: String): TieredEvChargeDigest {
    return TieredEvChargeDigest(
        code = code,
        name = name,
        region = "",
        channel = "",
        maximum = 0.0,
        lane = "",
        budget = 0.0,
        visible = false,
        windowDays = 0,
        status = TieredEvChargeDigestStatus.OPEN,
    )
}
