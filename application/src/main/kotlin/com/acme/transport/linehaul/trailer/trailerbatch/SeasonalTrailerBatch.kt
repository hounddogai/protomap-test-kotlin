package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer batch is in its life cycle. */
enum class SeasonalTrailerBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One seasonal trailer batch, with what the planners need to act on it. */
data class SeasonalTrailerBatch(
    var code: String,
    var title: String,
    var endsAt: Int,
    var trailerId: String,
    var weight: Double,
    var ownerId: String,
    var status: SeasonalTrailerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerBatchStatus): Boolean {
        when (status) {
            SeasonalTrailerBatchStatus.REQUESTED -> {
                when (next) {
                    SeasonalTrailerBatchStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalTrailerBatchStatus.GRANTED -> {
                when (next) {
                    SeasonalTrailerBatchStatus.REVOKED, SeasonalTrailerBatchStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalTrailerBatchStatus.REVOKED -> return next == SeasonalTrailerBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerBatchStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal trailer batch"} $code"

    fun projectedVolume(): Double = round((weight - endsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTrailerBatch {
        return SeasonalTrailerBatch(
            code = code,
            title = title,
            endsAt = endsAt,
            trailerId = trailerId,
            weight = weight,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal trailer batch in status requested. */
fun newSeasonalTrailerBatch(code: String, title: String): SeasonalTrailerBatch {
    return SeasonalTrailerBatch(
        code = code,
        title = title,
        endsAt = 0,
        trailerId = "",
        weight = 0.0,
        ownerId = "",
        status = SeasonalTrailerBatchStatus.REQUESTED,
    )
}
