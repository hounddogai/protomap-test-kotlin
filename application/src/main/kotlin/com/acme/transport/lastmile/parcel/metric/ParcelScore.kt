package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel score is in its life cycle. */
enum class ParcelScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A parcel score as the parcel team keeps it. */
data class ParcelScore(
    var code: String,
    var title: String,
    var active: Boolean,
    var batchSize: Int,
    var endsAt: Int,
    var retries: Int,
    var channel: String,
    var contactEmail: String,
    var lane: String,
    var timezone: String,
    var status: ParcelScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelScoreStatus): Boolean {
        when (status) {
            ParcelScoreStatus.OPEN -> {
                when (next) {
                    ParcelScoreStatus.RESOLVED -> return true
                }
                return false
            }
            ParcelScoreStatus.IN_REVIEW -> return next == ParcelScoreStatus.REOPENED || next == ParcelScoreStatus.ARCHIVED
            ParcelScoreStatus.RESOLVED -> return next == ParcelScoreStatus.REOPENED || next == ParcelScoreStatus.ARCHIVED
            ParcelScoreStatus.REOPENED -> {
                when (next) {
                    ParcelScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelScoreStatus) {
        if (!canMoveTo(next)) {
            error("parcel score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((batchSize.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelScore {
        return ParcelScore(
            code = code,
            title = title,
            active = active,
            batchSize = batchSize,
            endsAt = endsAt,
            retries = retries,
            channel = channel,
            contactEmail = contactEmail,
            lane = lane,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a parcel score in status open. */
fun newParcelScore(code: String, title: String): ParcelScore {
    return ParcelScore(
        code = code,
        title = title,
        active = false,
        batchSize = 0,
        endsAt = 0,
        retries = 0,
        channel = "",
        contactEmail = "",
        lane = "",
        timezone = "",
        status = ParcelScoreStatus.OPEN,
    )
}
