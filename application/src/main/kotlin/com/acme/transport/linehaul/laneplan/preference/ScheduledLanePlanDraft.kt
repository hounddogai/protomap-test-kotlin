package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lane plan draft is in its life cycle. */
enum class ScheduledLanePlanDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled lane plan draft and its current status. */
data class ScheduledLanePlanDraft(
    var code: String,
    var title: String,
    var sequence: Int,
    var carrier: String,
    var retries: Int,
    var odometer: Int,
    var status: ScheduledLanePlanDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanDraftStatus): Boolean {
        when (status) {
            ScheduledLanePlanDraftStatus.CREATED -> {
                when (next) {
                    ScheduledLanePlanDraftStatus.VALIDATED, ScheduledLanePlanDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledLanePlanDraftStatus.VALIDATED -> {
                when (next) {
                    ScheduledLanePlanDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledLanePlanDraftStatus.PUBLISHED -> {
                when (next) {
                    ScheduledLanePlanDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanDraftStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((sequence.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLanePlanDraft {
        return ScheduledLanePlanDraft(
            code = code,
            title = title,
            sequence = sequence,
            carrier = carrier,
            retries = retries,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled lane plan draft in status created. */
fun newScheduledLanePlanDraft(code: String, title: String): ScheduledLanePlanDraft {
    return ScheduledLanePlanDraft(
        code = code,
        title = title,
        sequence = 0,
        carrier = "",
        retries = 0,
        odometer = 0,
        status = ScheduledLanePlanDraftStatus.CREATED,
    )
}
