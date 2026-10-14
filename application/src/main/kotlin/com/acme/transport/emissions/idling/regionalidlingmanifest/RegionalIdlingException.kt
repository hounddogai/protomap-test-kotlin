package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional idling exception is in its life cycle. */
enum class RegionalIdlingExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional idling exception and its current status. */
data class RegionalIdlingException(
    var code: String,
    var title: String,
    var startsAt: Int,
    var score: Double,
    var ownerId: String,
    var description: String,
    var source: String,
    var status: RegionalIdlingExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIdlingExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIdlingExceptionStatus): Boolean {
        when (status) {
            RegionalIdlingExceptionStatus.PLANNED -> {
                when (next) {
                    RegionalIdlingExceptionStatus.IN_PROGRESS, RegionalIdlingExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalIdlingExceptionStatus.SCHEDULED -> return next == RegionalIdlingExceptionStatus.IN_PROGRESS || next == RegionalIdlingExceptionStatus.CANCELLED
            RegionalIdlingExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalIdlingExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalIdlingExceptionStatus.COMPLETED -> {
                when (next) {
                    RegionalIdlingExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIdlingExceptionStatus) {
        if (!canMoveTo(next)) {
            error("regional idling exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round(startsAt.toDouble() * score * 100) / 100

    fun snapshot(): RegionalIdlingException {
        return RegionalIdlingException(
            code = code,
            title = title,
            startsAt = startsAt,
            score = score,
            ownerId = ownerId,
            description = description,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional idling exception in status planned. */
fun newRegionalIdlingException(code: String, title: String): RegionalIdlingException {
    return RegionalIdlingException(
        code = code,
        title = title,
        startsAt = 0,
        score = 0.0,
        ownerId = "",
        description = "",
        source = "",
        status = RegionalIdlingExceptionStatus.PLANNED,
    )
}
