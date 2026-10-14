package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability draft is in its life cycle. */
enum class AvailabilityDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A availability draft. Its code is unique within a tenant. */
data class AvailabilityDraft(
    var code: String,
    var title: String,
    var source: String,
    var batchSize: Int,
    var limit: Int,
    var lane: String,
    var driverId: String,
    var summary: String,
    var amount: Double,
    var status: AvailabilityDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityDraftStatus): Boolean {
        when (status) {
            AvailabilityDraftStatus.PLANNED -> {
                when (next) {
                    AvailabilityDraftStatus.SCHEDULED -> return true
                }
                return false
            }
            AvailabilityDraftStatus.SCHEDULED -> {
                when (next) {
                    AvailabilityDraftStatus.IN_PROGRESS, AvailabilityDraftStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityDraftStatus.IN_PROGRESS -> {
                when (next) {
                    AvailabilityDraftStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityDraftStatus.COMPLETED -> {
                when (next) {
                    AvailabilityDraftStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityDraftStatus) {
        if (!canMoveTo(next)) {
            error("availability draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(limit.toDouble() * amount * 100) / 100

    fun snapshot(): AvailabilityDraft {
        return AvailabilityDraft(
            code = code,
            title = title,
            source = source,
            batchSize = batchSize,
            limit = limit,
            lane = lane,
            driverId = driverId,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a availability draft in status planned. */
fun newAvailabilityDraft(code: String, title: String): AvailabilityDraft {
    return AvailabilityDraft(
        code = code,
        title = title,
        source = "",
        batchSize = 0,
        limit = 0,
        lane = "",
        driverId = "",
        summary = "",
        amount = 0.0,
        status = AvailabilityDraftStatus.PLANNED,
    )
}
