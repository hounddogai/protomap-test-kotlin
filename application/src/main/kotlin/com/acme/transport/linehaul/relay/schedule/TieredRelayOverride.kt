package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered relay override is in its life cycle. */
enum class TieredRelayOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered relay override as the relay team keeps it. */
data class TieredRelayOverride(
    var code: String,
    var name: String,
    var loadKg: Double,
    var windowDays: Int,
    var title: String,
    var ceiling: Double,
    var batchSize: Int,
    var status: TieredRelayOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRelayOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRelayOverrideStatus): Boolean {
        when (status) {
            TieredRelayOverrideStatus.PROPOSED -> {
                when (next) {
                    TieredRelayOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredRelayOverrideStatus.APPROVED -> {
                when (next) {
                    TieredRelayOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredRelayOverrideStatus.REJECTED -> {
                when (next) {
                    TieredRelayOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRelayOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tiered relay override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((ceiling - batchSize.toDouble()) * 100) / 100

    fun snapshot(): TieredRelayOverride {
        return TieredRelayOverride(
            code = code,
            name = name,
            loadKg = loadKg,
            windowDays = windowDays,
            title = title,
            ceiling = ceiling,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered relay override in status proposed. */
fun newTieredRelayOverride(code: String, name: String): TieredRelayOverride {
    return TieredRelayOverride(
        code = code,
        name = name,
        loadKg = 0.0,
        windowDays = 0,
        title = "",
        ceiling = 0.0,
        batchSize = 0,
        status = TieredRelayOverrideStatus.PROPOSED,
    )
}
