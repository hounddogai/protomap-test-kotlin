package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone summary is in its life cycle. */
enum class ZoneSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a zone summary and its current status. */
data class ZoneSummary(
    var code: String,
    var title: String,
    var externalRef: String,
    var tags: MutableList<String>,
    var rate: Double,
    var capacity: Int,
    var locale: String,
    var floorValue: Double,
    var category: String,
    var status: ZoneSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneSummaryStatus): Boolean {
        when (status) {
            ZoneSummaryStatus.DRAFT -> {
                when (next) {
                    ZoneSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneSummaryStatus.PENDING -> {
                when (next) {
                    ZoneSummaryStatus.ACTIVE, ZoneSummaryStatus.CLOSED -> return true
                }
                return false
            }
            ZoneSummaryStatus.ACTIVE -> {
                when (next) {
                    ZoneSummaryStatus.SUSPENDED, ZoneSummaryStatus.CLOSED -> return true
                }
                return false
            }
            ZoneSummaryStatus.SUSPENDED -> return next == ZoneSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ZoneSummaryStatus) {
        if (!canMoveTo(next)) {
            error("zone summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((capacity.toDouble() - rate) * 100) / 100

    fun snapshot(): ZoneSummary {
        return ZoneSummary(
            code = code,
            title = title,
            externalRef = externalRef,
            tags = tags,
            rate = rate,
            capacity = capacity,
            locale = locale,
            floorValue = floorValue,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a zone summary in status draft. */
fun newZoneSummary(code: String, title: String): ZoneSummary {
    return ZoneSummary(
        code = code,
        title = title,
        externalRef = "",
        tags = mutableListOf<String>(),
        rate = 0.0,
        capacity = 0,
        locale = "",
        floorValue = 0.0,
        category = "",
        status = ZoneSummaryStatus.DRAFT,
    )
}
