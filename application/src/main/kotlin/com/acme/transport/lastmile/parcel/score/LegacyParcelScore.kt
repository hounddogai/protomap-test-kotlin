package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy parcel score records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a legacy parcel score is in its life cycle. */
enum class LegacyParcelScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy parcel score as the parcel team keeps it. */
data class LegacyParcelScore(
    var code: String,
    var title: String,
    var driverId: String,
    var reviewer: String,
    var ceiling: Double,
    var expiresAt: Int,
    var approved: Boolean,
    var limit: Int,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var status: LegacyParcelScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyParcelScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyParcelScoreStatus): Boolean {
        when (status) {
            LegacyParcelScoreStatus.CREATED -> {
                when (next) {
                    LegacyParcelScoreStatus.PUBLISHED, LegacyParcelScoreStatus.RETIRED -> return true
                }
                return false
            }
            LegacyParcelScoreStatus.VALIDATED -> {
                when (next) {
                    LegacyParcelScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyParcelScoreStatus.PUBLISHED -> {
                when (next) {
                    LegacyParcelScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyParcelScoreStatus) {
        if (!canMoveTo(next)) {
            error("legacy parcel score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy parcel score"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((limit.toDouble() - ceiling) * 100) / 100

    fun snapshot(): LegacyParcelScore {
        return LegacyParcelScore(
            code = code,
            title = title,
            driverId = driverId,
            reviewer = reviewer,
            ceiling = ceiling,
            expiresAt = expiresAt,
            approved = approved,
            limit = limit,
            dock = dock,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy parcel score in status created. */
fun newLegacyParcelScore(code: String, title: String): LegacyParcelScore {
    return LegacyParcelScore(
        code = code,
        title = title,
        driverId = "",
        reviewer = "",
        ceiling = 0.0,
        expiresAt = 0,
        approved = false,
        limit = 0,
        dock = "",
        attributes = mutableMapOf<String, String>(),
        status = LegacyParcelScoreStatus.CREATED,
    )
}
