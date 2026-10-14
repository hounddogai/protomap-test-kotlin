package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal backhaul manifest is in its life cycle. */
enum class SeasonalBackhaulManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal backhaul manifest as the backhaul team keeps it. */
data class SeasonalBackhaulManifest(
    var code: String,
    var label: String,
    var driverId: String,
    var cost: Double,
    var loadKg: Double,
    var amount: Double,
    var maximum: Double,
    var attempts: Int,
    var source: String,
    var tags: MutableList<String>,
    var title: String,
    var status: SeasonalBackhaulManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBackhaulManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBackhaulManifestStatus): Boolean {
        when (status) {
            SeasonalBackhaulManifestStatus.PROPOSED -> return next == SeasonalBackhaulManifestStatus.REJECTED || next == SeasonalBackhaulManifestStatus.WITHDRAWN
            SeasonalBackhaulManifestStatus.APPROVED -> {
                when (next) {
                    SeasonalBackhaulManifestStatus.REJECTED, SeasonalBackhaulManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalBackhaulManifestStatus.REJECTED -> {
                when (next) {
                    SeasonalBackhaulManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalBackhaulManifestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal backhaul manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal backhaul manifest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((cost + loadKg) * 100) / 100

    fun snapshot(): SeasonalBackhaulManifest {
        return SeasonalBackhaulManifest(
            code = code,
            label = label,
            driverId = driverId,
            cost = cost,
            loadKg = loadKg,
            amount = amount,
            maximum = maximum,
            attempts = attempts,
            source = source,
            tags = tags,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal backhaul manifest in status proposed. */
fun newSeasonalBackhaulManifest(code: String, label: String): SeasonalBackhaulManifest {
    return SeasonalBackhaulManifest(
        code = code,
        label = label,
        driverId = "",
        cost = 0.0,
        loadKg = 0.0,
        amount = 0.0,
        maximum = 0.0,
        attempts = 0,
        source = "",
        tags = mutableListOf<String>(),
        title = "",
        status = SeasonalBackhaulManifestStatus.PROPOSED,
    )
}
