package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive trailer yard mapping is in its life cycle. */
enum class AdaptiveTrailerYardMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive trailer yard mapping as the trailer yard team keeps it. */
data class AdaptiveTrailerYardMapping(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var summary: String,
    var minimum: Double,
    var expiresAt: Int,
    var archived: Boolean,
    var lane: String,
    var status: AdaptiveTrailerYardMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerYardMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerYardMappingStatus): Boolean {
        when (status) {
            AdaptiveTrailerYardMappingStatus.NEW -> {
                when (next) {
                    AdaptiveTrailerYardMappingStatus.QUEUED, AdaptiveTrailerYardMappingStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveTrailerYardMappingStatus.QUEUED -> {
                when (next) {
                    AdaptiveTrailerYardMappingStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveTrailerYardMappingStatus.RUNNING -> {
                when (next) {
                    AdaptiveTrailerYardMappingStatus.DONE, AdaptiveTrailerYardMappingStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveTrailerYardMappingStatus.DONE -> return next == AdaptiveTrailerYardMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerYardMappingStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer yard mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun effectiveTotal(): Double = round(minimum * expiresAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveTrailerYardMapping {
        return AdaptiveTrailerYardMapping(
            code = code,
            title = title,
            tags = tags,
            summary = summary,
            minimum = minimum,
            expiresAt = expiresAt,
            archived = archived,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive trailer yard mapping in status new. */
fun newAdaptiveTrailerYardMapping(code: String, title: String): AdaptiveTrailerYardMapping {
    return AdaptiveTrailerYardMapping(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        summary = "",
        minimum = 0.0,
        expiresAt = 0,
        archived = false,
        lane = "",
        status = AdaptiveTrailerYardMappingStatus.NEW,
    )
}
