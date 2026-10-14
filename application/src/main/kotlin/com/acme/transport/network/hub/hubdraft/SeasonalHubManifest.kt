package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal hub manifest is in its life cycle. */
enum class SeasonalHubManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal hub manifest as the hub team keeps it. */
data class SeasonalHubManifest(
    var code: String,
    var title: String,
    var name: String,
    var stopCount: Int,
    var sequence: Int,
    var approved: Boolean,
    var status: SeasonalHubManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalHubManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalHubManifestStatus): Boolean {
        when (status) {
            SeasonalHubManifestStatus.PROPOSED -> {
                when (next) {
                    SeasonalHubManifestStatus.APPROVED, SeasonalHubManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalHubManifestStatus.APPROVED -> {
                when (next) {
                    SeasonalHubManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalHubManifestStatus.REJECTED -> {
                when (next) {
                    SeasonalHubManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalHubManifestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal hub manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal hub manifest"} $code"

    fun projectedTotal(): Double = round((sequence.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): SeasonalHubManifest {
        return SeasonalHubManifest(
            code = code,
            title = title,
            name = name,
            stopCount = stopCount,
            sequence = sequence,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal hub manifest in status proposed. */
fun newSeasonalHubManifest(code: String, title: String): SeasonalHubManifest {
    return SeasonalHubManifest(
        code = code,
        title = title,
        name = "",
        stopCount = 0,
        sequence = 0,
        approved = false,
        status = SeasonalHubManifestStatus.PROPOSED,
    )
}
