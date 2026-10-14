package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global sortation draft is in its life cycle. */
enum class GlobalSortationDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global sortation draft, with what the planners need to act on it. */
data class GlobalSortationDraft(
    var code: String,
    var title: String,
    var ownerId: String,
    var phone: String,
    var distanceKm: Double,
    var trailerId: String,
    var carrier: String,
    var driverId: String,
    var cost: Double,
    var segment: String,
    var visible: Boolean,
    var status: GlobalSortationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSortationDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSortationDraftStatus): Boolean {
        when (status) {
            GlobalSortationDraftStatus.CREATED -> {
                when (next) {
                    GlobalSortationDraftStatus.VALIDATED -> return true
                }
                return false
            }
            GlobalSortationDraftStatus.VALIDATED -> {
                when (next) {
                    GlobalSortationDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalSortationDraftStatus.PUBLISHED -> {
                when (next) {
                    GlobalSortationDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalSortationDraftStatus) {
        if (!canMoveTo(next)) {
            error("global sortation draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((cost - distanceKm) * 100) / 100

    fun snapshot(): GlobalSortationDraft {
        return GlobalSortationDraft(
            code = code,
            title = title,
            ownerId = ownerId,
            phone = phone,
            distanceKm = distanceKm,
            trailerId = trailerId,
            carrier = carrier,
            driverId = driverId,
            cost = cost,
            segment = segment,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global sortation draft in status created. */
fun newGlobalSortationDraft(code: String, title: String): GlobalSortationDraft {
    return GlobalSortationDraft(
        code = code,
        title = title,
        ownerId = "",
        phone = "",
        distanceKm = 0.0,
        trailerId = "",
        carrier = "",
        driverId = "",
        cost = 0.0,
        segment = "",
        visible = false,
        status = GlobalSortationDraftStatus.CREATED,
    )
}
