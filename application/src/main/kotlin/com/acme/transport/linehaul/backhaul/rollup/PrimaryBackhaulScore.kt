package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary backhaul score is in its life cycle. */
enum class PrimaryBackhaulScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary backhaul score, with what the planners need to act on it. */
data class PrimaryBackhaulScore(
    var code: String,
    var title: String,
    var category: String,
    var label: String,
    var driverId: String,
    var channel: String,
    var pallets: Int,
    var revision: Int,
    var ownerId: String,
    var status: PrimaryBackhaulScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBackhaulScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBackhaulScoreStatus): Boolean {
        when (status) {
            PrimaryBackhaulScoreStatus.IDLE -> {
                when (next) {
                    PrimaryBackhaulScoreStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryBackhaulScoreStatus.WARMING -> {
                when (next) {
                    PrimaryBackhaulScoreStatus.READY -> return true
                }
                return false
            }
            PrimaryBackhaulScoreStatus.READY -> {
                when (next) {
                    PrimaryBackhaulScoreStatus.DRAINING, PrimaryBackhaulScoreStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryBackhaulScoreStatus.DRAINING -> return next == PrimaryBackhaulScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryBackhaulScoreStatus) {
        if (!canMoveTo(next)) {
            error("primary backhaul score $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $title"

    fun projectedVolume(): Double = round((pallets.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryBackhaulScore {
        return PrimaryBackhaulScore(
            code = code,
            title = title,
            category = category,
            label = label,
            driverId = driverId,
            channel = channel,
            pallets = pallets,
            revision = revision,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary backhaul score in status idle. */
fun newPrimaryBackhaulScore(code: String, title: String): PrimaryBackhaulScore {
    return PrimaryBackhaulScore(
        code = code,
        title = title,
        category = "",
        label = "",
        driverId = "",
        channel = "",
        pallets = 0,
        revision = 0,
        ownerId = "",
        status = PrimaryBackhaulScoreStatus.IDLE,
    )
}
