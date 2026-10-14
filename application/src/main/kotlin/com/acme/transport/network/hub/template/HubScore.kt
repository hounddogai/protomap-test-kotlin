package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub score is in its life cycle. */
enum class HubScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A hub score as the hub team keeps it. */
data class HubScore(
    var code: String,
    var label: String,
    var archived: Boolean,
    var cost: Double,
    var expiresAt: Int,
    var ownerId: String,
    var summary: String,
    var source: String,
    var threshold: Double,
    var status: HubScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubScoreStatus): Boolean {
        when (status) {
            HubScoreStatus.OPEN -> {
                when (next) {
                    HubScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            HubScoreStatus.IN_REVIEW -> {
                when (next) {
                    HubScoreStatus.RESOLVED, HubScoreStatus.REOPENED -> return true
                }
                return false
            }
            HubScoreStatus.RESOLVED -> return next == HubScoreStatus.REOPENED || next == HubScoreStatus.ARCHIVED
            HubScoreStatus.REOPENED -> {
                when (next) {
                    HubScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubScoreStatus) {
        if (!canMoveTo(next)) {
            error("hub score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((threshold - cost) * 100) / 100

    fun snapshot(): HubScore {
        return HubScore(
            code = code,
            label = label,
            archived = archived,
            cost = cost,
            expiresAt = expiresAt,
            ownerId = ownerId,
            summary = summary,
            source = source,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hub score in status open. */
fun newHubScore(code: String, label: String): HubScore {
    return HubScore(
        code = code,
        label = label,
        archived = false,
        cost = 0.0,
        expiresAt = 0,
        ownerId = "",
        summary = "",
        source = "",
        threshold = 0.0,
        status = HubScoreStatus.OPEN,
    )
}
