package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone digest records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone digest is in its life cycle. */
enum class ZoneDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A zone digest. Its code is unique within a tenant. */
data class ZoneDigest(
    var code: String,
    var title: String,
    var amount: Double,
    var quantity: Int,
    var approved: Boolean,
    var country: String,
    var archived: Boolean,
    var endsAt: Int,
    var maximum: Double,
    var status: ZoneDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneDigestStatus): Boolean {
        when (status) {
            ZoneDigestStatus.PLANNED -> return next == ZoneDigestStatus.IN_PROGRESS
            ZoneDigestStatus.SCHEDULED -> return next == ZoneDigestStatus.COMPLETED
            ZoneDigestStatus.IN_PROGRESS -> return next == ZoneDigestStatus.COMPLETED
            ZoneDigestStatus.COMPLETED -> {
                when (next) {
                    ZoneDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneDigestStatus) {
        if (!canMoveTo(next)) {
            error("zone digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((amount + quantity.toDouble()) * 100) / 100

    fun snapshot(): ZoneDigest {
        return ZoneDigest(
            code = code,
            title = title,
            amount = amount,
            quantity = quantity,
            approved = approved,
            country = country,
            archived = archived,
            endsAt = endsAt,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a zone digest in status planned. */
fun newZoneDigest(code: String, title: String): ZoneDigest {
    return ZoneDigest(
        code = code,
        title = title,
        amount = 0.0,
        quantity = 0,
        approved = false,
        country = "",
        archived = false,
        endsAt = 0,
        maximum = 0.0,
        status = ZoneDigestStatus.PLANNED,
    )
}
