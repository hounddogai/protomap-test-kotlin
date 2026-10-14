package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default cross dock preference is in its life cycle. */
enum class DefaultCrossDockPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default cross dock preference. Its code is unique within a tenant. */
data class DefaultCrossDockPreference(
    var code: String,
    var name: String,
    var category: String,
    var threshold: Double,
    var vehicleId: String,
    var createdAt: Int,
    var cost: Double,
    var visible: Boolean,
    var pallets: Int,
    var status: DefaultCrossDockPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCrossDockPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCrossDockPreferenceStatus): Boolean {
        when (status) {
            DefaultCrossDockPreferenceStatus.PROPOSED -> return next == DefaultCrossDockPreferenceStatus.WITHDRAWN
            DefaultCrossDockPreferenceStatus.APPROVED -> {
                when (next) {
                    DefaultCrossDockPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultCrossDockPreferenceStatus.REJECTED -> return next == DefaultCrossDockPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultCrossDockPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default cross dock preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((createdAt.toDouble() + threshold) * 100) / 100

    fun snapshot(): DefaultCrossDockPreference {
        return DefaultCrossDockPreference(
            code = code,
            name = name,
            category = category,
            threshold = threshold,
            vehicleId = vehicleId,
            createdAt = createdAt,
            cost = cost,
            visible = visible,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default cross dock preference in status proposed. */
fun newDefaultCrossDockPreference(code: String, name: String): DefaultCrossDockPreference {
    return DefaultCrossDockPreference(
        code = code,
        name = name,
        category = "",
        threshold = 0.0,
        vehicleId = "",
        createdAt = 0,
        cost = 0.0,
        visible = false,
        pallets = 0,
        status = DefaultCrossDockPreferenceStatus.PROPOSED,
    )
}
