package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive trailer manifest is in its life cycle. */
enum class AdaptiveTrailerManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive trailer manifest. Its code is unique within a tenant. */
data class AdaptiveTrailerManifest(
    var code: String,
    var title: String,
    var windowDays: Int,
    var limit: Int,
    var tags: MutableList<String>,
    var summary: String,
    var status: AdaptiveTrailerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerManifestStatus): Boolean {
        when (status) {
            AdaptiveTrailerManifestStatus.OPEN -> {
                when (next) {
                    AdaptiveTrailerManifestStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveTrailerManifestStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveTrailerManifestStatus.RESOLVED -> return true
                }
                return false
            }
            AdaptiveTrailerManifestStatus.RESOLVED -> return next == AdaptiveTrailerManifestStatus.REOPENED
            AdaptiveTrailerManifestStatus.REOPENED -> {
                when (next) {
                    AdaptiveTrailerManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(windowDays.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): AdaptiveTrailerManifest {
        return AdaptiveTrailerManifest(
            code = code,
            title = title,
            windowDays = windowDays,
            limit = limit,
            tags = tags,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive trailer manifest in status open. */
fun newAdaptiveTrailerManifest(code: String, title: String): AdaptiveTrailerManifest {
    return AdaptiveTrailerManifest(
        code = code,
        title = title,
        windowDays = 0,
        limit = 0,
        tags = mutableListOf<String>(),
        summary = "",
        status = AdaptiveTrailerManifestStatus.OPEN,
    )
}
