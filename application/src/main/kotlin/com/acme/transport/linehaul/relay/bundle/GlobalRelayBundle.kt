package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global relay bundle records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a global relay bundle is in its life cycle. */
enum class GlobalRelayBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global relay bundle and its current status. */
data class GlobalRelayBundle(
    var code: String,
    var label: String,
    var minimum: Double,
    var graceDays: Int,
    var expiresAt: Int,
    var visible: Boolean,
    var margin: Double,
    var description: String,
    var pallets: Int,
    var status: GlobalRelayBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRelayBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRelayBundleStatus): Boolean {
        when (status) {
            GlobalRelayBundleStatus.NEW -> {
                when (next) {
                    GlobalRelayBundleStatus.RUNNING, GlobalRelayBundleStatus.FAILED -> return true
                }
                return false
            }
            GlobalRelayBundleStatus.QUEUED -> {
                when (next) {
                    GlobalRelayBundleStatus.DONE, GlobalRelayBundleStatus.FAILED -> return true
                }
                return false
            }
            GlobalRelayBundleStatus.RUNNING -> {
                when (next) {
                    GlobalRelayBundleStatus.DONE, GlobalRelayBundleStatus.FAILED -> return true
                }
                return false
            }
            GlobalRelayBundleStatus.DONE -> {
                when (next) {
                    GlobalRelayBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRelayBundleStatus) {
        if (!canMoveTo(next)) {
            error("global relay bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((pallets.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalRelayBundle {
        return GlobalRelayBundle(
            code = code,
            label = label,
            minimum = minimum,
            graceDays = graceDays,
            expiresAt = expiresAt,
            visible = visible,
            margin = margin,
            description = description,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global relay bundle in status new. */
fun newGlobalRelayBundle(code: String, label: String): GlobalRelayBundle {
    return GlobalRelayBundle(
        code = code,
        label = label,
        minimum = 0.0,
        graceDays = 0,
        expiresAt = 0,
        visible = false,
        margin = 0.0,
        description = "",
        pallets = 0,
        status = GlobalRelayBundleStatus.NEW,
    )
}
