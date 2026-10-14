package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped roster notice is in its life cycle. */
enum class CappedRosterNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped roster notice as the roster team keeps it. */
data class CappedRosterNotice(
    var code: String,
    var label: String,
    var archived: Boolean,
    var cost: Double,
    var carrier: String,
    var etaMinutes: Int,
    var pallets: Int,
    var loadKg: Double,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var status: CappedRosterNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRosterNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRosterNoticeStatus): Boolean {
        when (status) {
            CappedRosterNoticeStatus.PROPOSED -> return next == CappedRosterNoticeStatus.APPROVED
            CappedRosterNoticeStatus.APPROVED -> return next == CappedRosterNoticeStatus.REJECTED || next == CappedRosterNoticeStatus.WITHDRAWN
            CappedRosterNoticeStatus.REJECTED -> return next == CappedRosterNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedRosterNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped roster notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round(cost * loadKg * 100) / 100

    fun snapshot(): CappedRosterNotice {
        return CappedRosterNotice(
            code = code,
            label = label,
            archived = archived,
            cost = cost,
            carrier = carrier,
            etaMinutes = etaMinutes,
            pallets = pallets,
            loadKg = loadKg,
            tags = tags,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped roster notice in status proposed. */
fun newCappedRosterNotice(code: String, label: String): CappedRosterNotice {
    return CappedRosterNotice(
        code = code,
        label = label,
        archived = false,
        cost = 0.0,
        carrier = "",
        etaMinutes = 0,
        pallets = 0,
        loadKg = 0.0,
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        status = CappedRosterNoticeStatus.PROPOSED,
    )
}
