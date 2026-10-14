package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire score is in its life cycle. */
enum class TireScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tire score, with what the planners need to act on it. */
data class TireScore(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var visible: Boolean,
    var ownerId: String,
    var tags: MutableList<String>,
    var phone: String,
    var budget: Double,
    var status: TireScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireScoreStatus): Boolean {
        when (status) {
            TireScoreStatus.NEW -> return next == TireScoreStatus.DONE || next == TireScoreStatus.FAILED
            TireScoreStatus.QUEUED -> {
                when (next) {
                    TireScoreStatus.RUNNING -> return true
                }
                return false
            }
            TireScoreStatus.RUNNING -> return next == TireScoreStatus.FAILED
            TireScoreStatus.DONE -> return next == TireScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TireScoreStatus) {
        if (!canMoveTo(next)) {
            error("tire score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((etaMinutes.toDouble() - budget) * 100) / 100

    fun snapshot(): TireScore {
        return TireScore(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            visible = visible,
            ownerId = ownerId,
            tags = tags,
            phone = phone,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tire score in status new. */
fun newTireScore(code: String, title: String): TireScore {
    return TireScore(
        code = code,
        title = title,
        etaMinutes = 0,
        visible = false,
        ownerId = "",
        tags = mutableListOf<String>(),
        phone = "",
        budget = 0.0,
        status = TireScoreStatus.NEW,
    )
}
