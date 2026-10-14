package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient lane plan slot is in its life cycle. */
enum class LenientLanePlanSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient lane plan slot as the lane plan team keeps it. */
data class LenientLanePlanSlot(
    var code: String,
    var title: String,
    var lane: String,
    var endsAt: Int,
    var startsAt: Int,
    var trailerId: String,
    var retries: Int,
    var reviewer: String,
    var currency: String,
    var channel: String,
    var status: LenientLanePlanSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLanePlanSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLanePlanSlotStatus): Boolean {
        when (status) {
            LenientLanePlanSlotStatus.CREATED -> return next == LenientLanePlanSlotStatus.VALIDATED || next == LenientLanePlanSlotStatus.RETIRED
            LenientLanePlanSlotStatus.VALIDATED -> return next == LenientLanePlanSlotStatus.RETIRED
            LenientLanePlanSlotStatus.PUBLISHED -> {
                when (next) {
                    LenientLanePlanSlotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLanePlanSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient lane plan slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round(retries.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): LenientLanePlanSlot {
        return LenientLanePlanSlot(
            code = code,
            title = title,
            lane = lane,
            endsAt = endsAt,
            startsAt = startsAt,
            trailerId = trailerId,
            retries = retries,
            reviewer = reviewer,
            currency = currency,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient lane plan slot in status created. */
fun newLenientLanePlanSlot(code: String, title: String): LenientLanePlanSlot {
    return LenientLanePlanSlot(
        code = code,
        title = title,
        lane = "",
        endsAt = 0,
        startsAt = 0,
        trailerId = "",
        retries = 0,
        reviewer = "",
        currency = "",
        channel = "",
        status = LenientLanePlanSlotStatus.CREATED,
    )
}
