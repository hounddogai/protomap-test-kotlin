package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered clearance draft is in its life cycle. */
enum class TieredClearanceDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tiered clearance draft, with what the planners need to act on it. */
data class TieredClearanceDraft(
    var code: String,
    var title: String,
    var odometer: Int,
    var discount: Double,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var lane: String,
    var timezone: String,
    var segment: String,
    var checksum: String,
    var status: TieredClearanceDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredClearanceDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredClearanceDraftStatus): Boolean {
        when (status) {
            TieredClearanceDraftStatus.DRAFT -> return next == TieredClearanceDraftStatus.SUSPENDED
            TieredClearanceDraftStatus.PENDING -> return next == TieredClearanceDraftStatus.CLOSED
            TieredClearanceDraftStatus.ACTIVE -> {
                when (next) {
                    TieredClearanceDraftStatus.CLOSED -> return true
                }
                return false
            }
            TieredClearanceDraftStatus.SUSPENDED -> return next == TieredClearanceDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredClearanceDraftStatus) {
        if (!canMoveTo(next)) {
            error("tiered clearance draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered clearance draft"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((expiresAt.toDouble() - discount) * 100) / 100

    fun snapshot(): TieredClearanceDraft {
        return TieredClearanceDraft(
            code = code,
            title = title,
            odometer = odometer,
            discount = discount,
            expiresAt = expiresAt,
            attributes = attributes,
            ownerId = ownerId,
            lane = lane,
            timezone = timezone,
            segment = segment,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered clearance draft in status draft. */
fun newTieredClearanceDraft(code: String, title: String): TieredClearanceDraft {
    return TieredClearanceDraft(
        code = code,
        title = title,
        odometer = 0,
        discount = 0.0,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        lane = "",
        timezone = "",
        segment = "",
        checksum = "",
        status = TieredClearanceDraftStatus.DRAFT,
    )
}
