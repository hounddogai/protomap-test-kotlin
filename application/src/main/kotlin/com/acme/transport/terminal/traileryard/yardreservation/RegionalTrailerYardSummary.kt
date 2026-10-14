package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional trailer yard summary is in its life cycle. */
enum class RegionalTrailerYardSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One regional trailer yard summary, with what the planners need to act on it. */
data class RegionalTrailerYardSummary(
    var code: String,
    var title: String,
    var minimum: Double,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var margin: Double,
    var driverId: String,
    var distanceKm: Double,
    var notes: String,
    var category: String,
    var version: Int,
    var status: RegionalTrailerYardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardSummaryStatus): Boolean {
        when (status) {
            RegionalTrailerYardSummaryStatus.IDLE -> {
                when (next) {
                    RegionalTrailerYardSummaryStatus.WARMING -> return true
                }
                return false
            }
            RegionalTrailerYardSummaryStatus.WARMING -> {
                when (next) {
                    RegionalTrailerYardSummaryStatus.STOPPED -> return true
                }
                return false
            }
            RegionalTrailerYardSummaryStatus.READY -> {
                when (next) {
                    RegionalTrailerYardSummaryStatus.DRAINING, RegionalTrailerYardSummaryStatus.STOPPED -> return true
                }
                return false
            }
            RegionalTrailerYardSummaryStatus.DRAINING -> {
                when (next) {
                    RegionalTrailerYardSummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(minimum * distanceKm * 100) / 100

    fun snapshot(): RegionalTrailerYardSummary {
        return RegionalTrailerYardSummary(
            code = code,
            title = title,
            minimum = minimum,
            attributes = attributes,
            contactEmail = contactEmail,
            margin = margin,
            driverId = driverId,
            distanceKm = distanceKm,
            notes = notes,
            category = category,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional trailer yard summary in status idle. */
fun newRegionalTrailerYardSummary(code: String, title: String): RegionalTrailerYardSummary {
    return RegionalTrailerYardSummary(
        code = code,
        title = title,
        minimum = 0.0,
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        margin = 0.0,
        driverId = "",
        distanceKm = 0.0,
        notes = "",
        category = "",
        version = 0,
        status = RegionalTrailerYardSummaryStatus.IDLE,
    )
}
