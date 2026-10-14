package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive node override records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a adaptive node override is in its life cycle. */
enum class AdaptiveNodeOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive node override. Its code is unique within a tenant. */
data class AdaptiveNodeOverride(
    var code: String,
    var title: String,
    var shiftCode: String,
    var loadKg: Double,
    var startsAt: Int,
    var locale: String,
    var durationMinutes: Int,
    var pallets: Int,
    var batchSize: Int,
    var fuelLiters: Double,
    var revision: Int,
    var status: AdaptiveNodeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNodeOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNodeOverrideStatus): Boolean {
        when (status) {
            AdaptiveNodeOverrideStatus.DRAFT -> {
                when (next) {
                    AdaptiveNodeOverrideStatus.PENDING, AdaptiveNodeOverrideStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveNodeOverrideStatus.PENDING -> return next == AdaptiveNodeOverrideStatus.SUSPENDED
            AdaptiveNodeOverrideStatus.ACTIVE -> return next == AdaptiveNodeOverrideStatus.SUSPENDED || next == AdaptiveNodeOverrideStatus.CLOSED
            AdaptiveNodeOverrideStatus.SUSPENDED -> return next == AdaptiveNodeOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveNodeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("adaptive node override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round(startsAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): AdaptiveNodeOverride {
        return AdaptiveNodeOverride(
            code = code,
            title = title,
            shiftCode = shiftCode,
            loadKg = loadKg,
            startsAt = startsAt,
            locale = locale,
            durationMinutes = durationMinutes,
            pallets = pallets,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive node override in status draft. */
fun newAdaptiveNodeOverride(code: String, title: String): AdaptiveNodeOverride {
    return AdaptiveNodeOverride(
        code = code,
        title = title,
        shiftCode = "",
        loadKg = 0.0,
        startsAt = 0,
        locale = "",
        durationMinutes = 0,
        pallets = 0,
        batchSize = 0,
        fuelLiters = 0.0,
        revision = 0,
        status = AdaptiveNodeOverrideStatus.DRAFT,
    )
}
