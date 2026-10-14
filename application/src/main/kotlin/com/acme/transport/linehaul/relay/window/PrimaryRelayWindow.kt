package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary relay window records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a primary relay window is in its life cycle. */
enum class PrimaryRelayWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary relay window and its current status. */
data class PrimaryRelayWindow(
    var code: String,
    var name: String,
    var version: Int,
    var updatedAt: Int,
    var batchSize: Int,
    var region: String,
    var status: PrimaryRelayWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRelayWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRelayWindowStatus): Boolean {
        when (status) {
            PrimaryRelayWindowStatus.OPEN -> {
                when (next) {
                    PrimaryRelayWindowStatus.IN_REVIEW, PrimaryRelayWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryRelayWindowStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryRelayWindowStatus.RESOLVED, PrimaryRelayWindowStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryRelayWindowStatus.RESOLVED -> {
                when (next) {
                    PrimaryRelayWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryRelayWindowStatus.REOPENED -> {
                when (next) {
                    PrimaryRelayWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRelayWindowStatus) {
        if (!canMoveTo(next)) {
            error("primary relay window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round((batchSize.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRelayWindow {
        return PrimaryRelayWindow(
            code = code,
            name = name,
            version = version,
            updatedAt = updatedAt,
            batchSize = batchSize,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary relay window in status open. */
fun newPrimaryRelayWindow(code: String, name: String): PrimaryRelayWindow {
    return PrimaryRelayWindow(
        code = code,
        name = name,
        version = 0,
        updatedAt = 0,
        batchSize = 0,
        region = "",
        status = PrimaryRelayWindowStatus.OPEN,
    )
}
