package com.acme.transport.network.hub.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered hub exception records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a tiered hub exception is in its life cycle. */
enum class TieredHubExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered hub exception as the hub team keeps it. */
data class TieredHubException(
    var code: String,
    var name: String,
    var windowDays: Int,
    var tags: MutableList<String>,
    var visible: Boolean,
    var lane: String,
    var sequence: Int,
    var endsAt: Int,
    var maximum: Double,
    var margin: Double,
    var status: TieredHubExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredHubExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredHubExceptionStatus): Boolean {
        when (status) {
            TieredHubExceptionStatus.CREATED -> return next == TieredHubExceptionStatus.RETIRED
            TieredHubExceptionStatus.VALIDATED -> {
                when (next) {
                    TieredHubExceptionStatus.PUBLISHED, TieredHubExceptionStatus.RETIRED -> return true
                }
                return false
            }
            TieredHubExceptionStatus.PUBLISHED -> return next == TieredHubExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredHubExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tiered hub exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round(margin * maximum * 100) / 100

    fun snapshot(): TieredHubException {
        return TieredHubException(
            code = code,
            name = name,
            windowDays = windowDays,
            tags = tags,
            visible = visible,
            lane = lane,
            sequence = sequence,
            endsAt = endsAt,
            maximum = maximum,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered hub exception in status created. */
fun newTieredHubException(code: String, name: String): TieredHubException {
    return TieredHubException(
        code = code,
        name = name,
        windowDays = 0,
        tags = mutableListOf<String>(),
        visible = false,
        lane = "",
        sequence = 0,
        endsAt = 0,
        maximum = 0.0,
        margin = 0.0,
        status = TieredHubExceptionStatus.CREATED,
    )
}
