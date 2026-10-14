package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default hub mapping is in its life cycle. */
enum class DefaultHubMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default hub mapping and its current status. */
data class DefaultHubMapping(
    var code: String,
    var title: String,
    var region: String,
    var durationMinutes: Int,
    var visible: Boolean,
    var endsAt: Int,
    var pinned: Boolean,
    var reviewer: String,
    var channel: String,
    var segment: String,
    var currency: String,
    var status: DefaultHubMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubMappingStatus): Boolean {
        when (status) {
            DefaultHubMappingStatus.OPEN -> return next == DefaultHubMappingStatus.IN_REVIEW
            DefaultHubMappingStatus.IN_REVIEW -> return next == DefaultHubMappingStatus.RESOLVED || next == DefaultHubMappingStatus.REOPENED
            DefaultHubMappingStatus.RESOLVED -> {
                when (next) {
                    DefaultHubMappingStatus.REOPENED, DefaultHubMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultHubMappingStatus.REOPENED -> {
                when (next) {
                    DefaultHubMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultHubMappingStatus) {
        if (!canMoveTo(next)) {
            error("default hub mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((durationMinutes.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultHubMapping {
        return DefaultHubMapping(
            code = code,
            title = title,
            region = region,
            durationMinutes = durationMinutes,
            visible = visible,
            endsAt = endsAt,
            pinned = pinned,
            reviewer = reviewer,
            channel = channel,
            segment = segment,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default hub mapping in status open. */
fun newDefaultHubMapping(code: String, title: String): DefaultHubMapping {
    return DefaultHubMapping(
        code = code,
        title = title,
        region = "",
        durationMinutes = 0,
        visible = false,
        endsAt = 0,
        pinned = false,
        reviewer = "",
        channel = "",
        segment = "",
        currency = "",
        status = DefaultHubMappingStatus.OPEN,
    )
}
