package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer draft is in its life cycle. */
enum class ManualTrailerDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual trailer draft. Its code is unique within a tenant. */
data class ManualTrailerDraft(
    var code: String,
    var label: String,
    var priority: Int,
    var lane: String,
    var routeCode: String,
    var rate: Double,
    var fuelLiters: Double,
    var source: String,
    var cost: Double,
    var status: ManualTrailerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerDraftStatus): Boolean {
        when (status) {
            ManualTrailerDraftStatus.REQUESTED -> {
                when (next) {
                    ManualTrailerDraftStatus.GRANTED, ManualTrailerDraftStatus.EXPIRED -> return true
                }
                return false
            }
            ManualTrailerDraftStatus.GRANTED -> {
                when (next) {
                    ManualTrailerDraftStatus.EXPIRED -> return true
                }
                return false
            }
            ManualTrailerDraftStatus.REVOKED -> {
                when (next) {
                    ManualTrailerDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerDraftStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((priority.toDouble() + cost) * 100) / 100

    fun snapshot(): ManualTrailerDraft {
        return ManualTrailerDraft(
            code = code,
            label = label,
            priority = priority,
            lane = lane,
            routeCode = routeCode,
            rate = rate,
            fuelLiters = fuelLiters,
            source = source,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual trailer draft in status requested. */
fun newManualTrailerDraft(code: String, label: String): ManualTrailerDraft {
    return ManualTrailerDraft(
        code = code,
        label = label,
        priority = 0,
        lane = "",
        routeCode = "",
        rate = 0.0,
        fuelLiters = 0.0,
        source = "",
        cost = 0.0,
        status = ManualTrailerDraftStatus.REQUESTED,
    )
}
