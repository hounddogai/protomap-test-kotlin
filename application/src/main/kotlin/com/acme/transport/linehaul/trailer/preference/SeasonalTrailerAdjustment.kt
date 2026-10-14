package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer adjustment is in its life cycle. */
enum class SeasonalTrailerAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal trailer adjustment, with what the planners need to act on it. */
data class SeasonalTrailerAdjustment(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var limit: Int,
    var approved: Boolean,
    var contactEmail: String,
    var attempts: Int,
    var tags: MutableList<String>,
    var createdAt: Int,
    var status: SeasonalTrailerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerAdjustmentStatus): Boolean {
        when (status) {
            SeasonalTrailerAdjustmentStatus.PLANNED -> {
                when (next) {
                    SeasonalTrailerAdjustmentStatus.COMPLETED, SeasonalTrailerAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalTrailerAdjustmentStatus.SCHEDULED -> return next == SeasonalTrailerAdjustmentStatus.COMPLETED || next == SeasonalTrailerAdjustmentStatus.CANCELLED
            SeasonalTrailerAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalTrailerAdjustmentStatus.COMPLETED, SeasonalTrailerAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalTrailerAdjustmentStatus.COMPLETED -> {
                when (next) {
                    SeasonalTrailerAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((limit.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTrailerAdjustment {
        return SeasonalTrailerAdjustment(
            code = code,
            title = title,
            attributes = attributes,
            etaMinutes = etaMinutes,
            limit = limit,
            approved = approved,
            contactEmail = contactEmail,
            attempts = attempts,
            tags = tags,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal trailer adjustment in status planned. */
fun newSeasonalTrailerAdjustment(code: String, title: String): SeasonalTrailerAdjustment {
    return SeasonalTrailerAdjustment(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        limit = 0,
        approved = false,
        contactEmail = "",
        attempts = 0,
        tags = mutableListOf<String>(),
        createdAt = 0,
        status = SeasonalTrailerAdjustmentStatus.PLANNED,
    )
}
