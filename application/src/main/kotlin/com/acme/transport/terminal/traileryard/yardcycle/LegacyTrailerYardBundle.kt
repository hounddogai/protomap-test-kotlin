package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy trailer yard bundle is in its life cycle. */
enum class LegacyTrailerYardBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy trailer yard bundle, with what the planners need to act on it. */
data class LegacyTrailerYardBundle(
    var code: String,
    var name: String,
    var reviewer: String,
    var dock: String,
    var margin: Double,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var carrier: String,
    var channel: String,
    var driverId: String,
    var status: LegacyTrailerYardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardBundleStatus): Boolean {
        when (status) {
            LegacyTrailerYardBundleStatus.NEW -> return next == LegacyTrailerYardBundleStatus.FAILED
            LegacyTrailerYardBundleStatus.QUEUED -> {
                when (next) {
                    LegacyTrailerYardBundleStatus.RUNNING, LegacyTrailerYardBundleStatus.DONE -> return true
                }
                return false
            }
            LegacyTrailerYardBundleStatus.RUNNING -> return next == LegacyTrailerYardBundleStatus.DONE
            LegacyTrailerYardBundleStatus.DONE -> {
                when (next) {
                    LegacyTrailerYardBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardBundleStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard bundle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((cost + version.toDouble()) * 100) / 100

    fun snapshot(): LegacyTrailerYardBundle {
        return LegacyTrailerYardBundle(
            code = code,
            name = name,
            reviewer = reviewer,
            dock = dock,
            margin = margin,
            cost = cost,
            attributes = attributes,
            version = version,
            carrier = carrier,
            channel = channel,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy trailer yard bundle in status new. */
fun newLegacyTrailerYardBundle(code: String, name: String): LegacyTrailerYardBundle {
    return LegacyTrailerYardBundle(
        code = code,
        name = name,
        reviewer = "",
        dock = "",
        margin = 0.0,
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        version = 0,
        carrier = "",
        channel = "",
        driverId = "",
        status = LegacyTrailerYardBundleStatus.NEW,
    )
}
