package com.acme.transport.network.hub.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hub manifest is in its life cycle. */
enum class WeightedHubManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted hub manifest as the hub team keeps it. */
data class WeightedHubManifest(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var country: String,
    var source: String,
    var stopCount: Int,
    var cost: Double,
    var etaMinutes: Int,
    var locale: String,
    var status: WeightedHubManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHubManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHubManifestStatus): Boolean {
        when (status) {
            WeightedHubManifestStatus.OPEN -> {
                when (next) {
                    WeightedHubManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedHubManifestStatus.IN_REVIEW -> {
                when (next) {
                    WeightedHubManifestStatus.RESOLVED -> return true
                }
                return false
            }
            WeightedHubManifestStatus.RESOLVED -> return next == WeightedHubManifestStatus.ARCHIVED
            WeightedHubManifestStatus.REOPENED -> {
                when (next) {
                    WeightedHubManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedHubManifestStatus) {
        if (!canMoveTo(next)) {
            error("weighted hub manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted hub manifest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((cost - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedHubManifest {
        return WeightedHubManifest(
            code = code,
            name = name,
            tags = tags,
            currency = currency,
            attributes = attributes,
            country = country,
            source = source,
            stopCount = stopCount,
            cost = cost,
            etaMinutes = etaMinutes,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted hub manifest in status open. */
fun newWeightedHubManifest(code: String, name: String): WeightedHubManifest {
    return WeightedHubManifest(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        currency = "",
        attributes = mutableMapOf<String, String>(),
        country = "",
        source = "",
        stopCount = 0,
        cost = 0.0,
        etaMinutes = 0,
        locale = "",
        status = WeightedHubManifestStatus.OPEN,
    )
}
