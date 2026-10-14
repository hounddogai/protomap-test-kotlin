package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped roster manifest is in its life cycle. */
enum class CappedRosterManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped roster manifest as the roster team keeps it. */
data class CappedRosterManifest(
    var code: String,
    var title: String,
    var contactEmail: String,
    var archived: Boolean,
    var driverId: String,
    var loadKg: Double,
    var category: String,
    var amount: Double,
    var cost: Double,
    var label: String,
    var notes: String,
    var status: CappedRosterManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRosterManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRosterManifestStatus): Boolean {
        when (status) {
            CappedRosterManifestStatus.PLANNED -> return next == CappedRosterManifestStatus.SCHEDULED || next == CappedRosterManifestStatus.IN_PROGRESS
            CappedRosterManifestStatus.SCHEDULED -> return next == CappedRosterManifestStatus.COMPLETED
            CappedRosterManifestStatus.IN_PROGRESS -> return next == CappedRosterManifestStatus.COMPLETED
            CappedRosterManifestStatus.COMPLETED -> {
                when (next) {
                    CappedRosterManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRosterManifestStatus) {
        if (!canMoveTo(next)) {
            error("capped roster manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun adjustedLoad(): Double = round(cost * amount * 100) / 100

    fun snapshot(): CappedRosterManifest {
        return CappedRosterManifest(
            code = code,
            title = title,
            contactEmail = contactEmail,
            archived = archived,
            driverId = driverId,
            loadKg = loadKg,
            category = category,
            amount = amount,
            cost = cost,
            label = label,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped roster manifest in status planned. */
fun newCappedRosterManifest(code: String, title: String): CappedRosterManifest {
    return CappedRosterManifest(
        code = code,
        title = title,
        contactEmail = "",
        archived = false,
        driverId = "",
        loadKg = 0.0,
        category = "",
        amount = 0.0,
        cost = 0.0,
        label = "",
        notes = "",
        status = CappedRosterManifestStatus.PLANNED,
    )
}
